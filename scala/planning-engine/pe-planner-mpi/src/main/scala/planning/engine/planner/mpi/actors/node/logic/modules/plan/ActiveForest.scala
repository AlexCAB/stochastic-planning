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
| created: 2026-10-10 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic.modules.plan

import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.errors.assertSameSize
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.plan.HuangWeight
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.node.logic.Actor
import planning.engine.planner.mpi.actors.node.logic.math.PuCalculation

private[node] trait ActiveForest extends PuCalculation:
  self: Actor.type =>
  import Message.*

  private def rootFound[F[_]: S](path: ActivePath)(using d: Def, ctx: Ctx): F[Unit] =
    for
      _ <- d.actors.planner.addActivePath(path)
      _ <- logInfo(s"[doActivation] Root found for path = $path")
    yield ()

  private def propagateFurther[F[_]: S](
      links: List[Node.Abs],
      path: ActivePath,
      time: IoTime,
      sampleIds: Set[SampleId],
  )(using Ctx): F[Unit] =
    for
      weights <- HuangWeight.init.div(links.size)
      _ <- logInfo(s"[doActivation] links = $links, weights = $weights, time = $time, sampleIds = $sampleIds")
      _ <- links.assertSameSize(weights, "Number of outLinks and weights must match.")
      paths = links.zip(weights).map((n, w) => n -> path.withNextWeight(w))
      _ <- logInfo(s"[doActivation] paths = $paths")
      _ <- paths.traverse_((n, ap) => n.activation(time, sampleIds, ap))
      _ <- logInfo(s"[doActivation] Sent ${links.size} activation messages to outLinks: $links")
    yield ()

  private[node] def doActivationCon[F[_]: S](msg: ActivationCon, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      pu <- calcInitPu(msg, state)
      _ <- logInfo(s"[doActivationCon] time = ${msg.time}, utility = ${msg.utility}, calculated PU = $pu")
      _ <- state.plan.activeSteps.assertEmpty("Active steps must be empty before activation.")
      (activeSteps, newState) <- state.mapPlanR(_.withActiveSteps(d.id, msg.time))
      sampleIds = state.struct.sampleMap.keySet // For active concrete nodes, we select all sample IDs.
      outLinks <- state.struct.lookup.allOutAbsLink.map(_.toList)
      self <- d.self
      path = ActivePath.init(self, activeSteps.map(_.key), pu)
      _ <- logInfo(s"[doActivationCon] steps = $activeSteps, outLinks = $outLinks, path = $path")
      _ <- if outLinks.nonEmpty then propagateFurther(outLinks, path, msg.time, sampleIds) else rootFound(path)
    yield newState.mapPlan(_.withPu(pu))

  private[node] def doActivationAbs[F[_]: S](msg: ActivationAbs, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      pu <- calcNextPu(msg, state)
      _ <- logInfo(s"[doActivationAbs] time = ${msg.time}, sampleIds = ${msg.sampleIds}")
      (activeSteps, newState) <- state.mapPlanR(_.withActiveStepsIfNotActivated(d.id, msg.time))
      outLinks <- state.struct.lookup.findOutAbsLink(msg.sampleIds).map(_.toList)
      self <- d.self
      path <- msg.path.append(self, activeSteps.map(_.key), pu)
      _ <- logInfo(s"[doActivationAbs] steps = $activeSteps, outLinks = $outLinks, path = ${msg.path} -> $path")
      _ <- if outLinks.nonEmpty then propagateFurther(outLinks, path, msg.time, msg.sampleIds) else rootFound(path)
    yield newState.mapPlan(_.withPu(pu))
