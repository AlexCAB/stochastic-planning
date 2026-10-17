/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 30.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.node.data.state

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.errors.*
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.data.parts.{Step, StepNode}
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.repr.Representable

private[node] final case class Plan(
    // Steps is a nodes of plan graph, belonging to this map network node.
    steps: Map[StepKey, Step],

    // Next path id is used to generate new unique plan path ID.
    nextPathId: Long,

    // Evaluated P(N) * U(N), probability and utility of this map network node.
    pu: Option[PU],
) extends Representable:
  import Step.*

  lazy val nextSteps: Map[StepKey, Next] = steps.collect { case (k, s: Next) => k -> s }
  lazy val activeSteps: Map[StepKey, Active] = steps.collect { case (k, s: Active) => k -> s }

  // Add new Active step, which is the root of the new sequence tree.
  // One map network can have not more than one active root.
  def withNewActiveRoot[F[_]: MT](mnId: MnId, time: IoTime): F[(StepNode, Plan)] =
    for
      key <- StepKey.init(mnId, nextPathId).pure
      _ <- steps.keySet.assertNotContain(key, s"Plan already contains step with key: $key")
    yield (
      StepNode(key, Active.init(time)),
      copy(steps = steps + (key -> Active.init(time)), nextPathId = nextPathId + 1),
    )

  // Activate Next steps if their time matches the given time.
  def withActiveNextSteps[F[_]: MT](mnId: MnId, time: IoTime): F[(Set[StepNode], Plan)] =
    for
      activeSteps <- nextSteps.filter((_, s) => s.time == time).view.mapValues(_.active).toMap.pure
      _ <- activeSteps.keySet.foreachM(k => k.mnId.assertEquals(mnId, s"StepKey mnId mismatch for: $k"))
    yield (
      activeSteps.map((k, s) => StepNode(k, s)).toSet,
      copy(steps = steps ++ activeSteps),
    )

  // Find and activate Next steps if their time matches the given time.
  // If no Next steps found, then add new Active step as the root of the new sequence tree.
  // See description in `Planner.tick` method and `Node.activationa` methods.
  def withActiveSteps[F[_]: MT](mnId: MnId, time: IoTime): F[(Set[StepNode], Plan)] =
    def addNewIfNotFound(p: Plan, found: Set[StepNode]): F[(Set[StepNode], Plan)] =
      if found.nonEmpty then (found, p).pure
      else p.withNewActiveRoot(mnId, time).map((newRoot, newPlan) => (Set(newRoot), newPlan))

    for
      (nextActive, planNa) <- this.withActiveNextSteps(mnId, time)
      (stepsActive, planSa) <- addNewIfNotFound(planNa, nextActive)
    yield (stepsActive, planSa)

  // If active steps was added earlier (by prev activation message), then return them.
  // Otherwise, find and activate Next steps if their time matches the given time.
  def withActiveStepsIfNotActivated[F[_]: MT](mnId: MnId, time: IoTime): F[(Set[StepNode], Plan)] =
    if activeSteps.nonEmpty then (activeSteps.map((k, s) => StepNode(k, s)).toSet, this).pure
    else withActiveSteps(mnId, time)

  def withPu(pu: PU): Plan = copy(pu = Some(pu))

private[node] object Plan:
  val init = Plan(steps = Map.empty, nextPathId = 1L, pu = None)
