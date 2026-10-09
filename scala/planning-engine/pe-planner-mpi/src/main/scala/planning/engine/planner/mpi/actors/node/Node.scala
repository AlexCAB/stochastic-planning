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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node

import cats.syntax.all.*
import cats.syntax.ext.*
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.data.Definition
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.node.{NodeData, StepKey}
import planning.engine.planner.mpi.model.data.samples.Sample

private[mpi] trait Node:
  def mnId: MnId
  def name: Option[HnName]

  def repr: String

  // Upsert source end of an edge to this node.
  def upsertEdgeSrc[F[_]: MT](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit]

  // Upsert target end of an edge to this node.
  def upsertEdgeTrg[F[_]: MT](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit]

  // Propagate the activation signal from concrete node (leafs of abstraction tree)
  // to higher abstract node (to the roots):
  //  1. Propagate link activation signal in the abstraction tree: Send LinkActivation message
  //     to target nodes if all outcoming LINK edges, if there is some.
  //  2. Search for Next steps in this node plan state and replace them with the Done steps.
  //  3. In case of no Next steps found, then add one Done steps, which will be a root of new plan tree. In some Next
  //     steps found (at 2), do not add a new Done steps (all possible plan paths should be covered by existing steps).
  //  4. Propagate then activation signal in the sequence tree: For each found or created Done steps (in 2 or 3),
  //     send the ThenActivation message to the target node of each none empty (i.e. that have samples) THEN
  //     outgoing edge from this node.
  def linkActivation[F[_]: MT](prev: StepKey, next: StepKey): F[Unit]

  // Propagate the activation signal from the current active node (which was activated by LinkActivation message),
  // to the next node in the sequence tree (i.e. to the nodes which likely will be activated
  // on the next world tick/step). Or in other words, move context boundary one step forward:
  //  1. If Planned steps exist in this node plan state then replace it with Next step.
  //  2. If no Planned steps exist, then build and add new one.
  def thenActivation[F[_]: MT](prev: StepKey, next: StepKey): F[Unit]

  // TODO
  def inference[F[_]: MT](prev: StepKey, next: StepKey): F[Unit]

  // TODO
  def planning[F[_]: MT](prev: StepKey, next: StepKey): F[Unit]

  override def toString: String = repr

private[mpi] object Node:
  type Msg = Actor.Msg

  trait Con extends Node:
    def mnId: MnId.Con
    def name: Option[HnName]
    def ioValue: IoValue

    lazy val repr: String = s"[${mnId.reprValue}, ${name.repr}, ${ioValue.repr}]"

  trait Abs extends Node:
    def mnId: MnId.Abs
    def name: Option[HnName]

    lazy val repr: String = s"(${mnId.reprValue}, ${name.repr})"

  def spawn[F[_]: MT](
      mnId: MnId,
      data: NodeData,
      manager: Manager,
      visualizer: Option[Visualizer],
      planner: Planner,
      ctx: ActorContext[?],
  ): F[Node] =
    for
      definition <- Definition(mnId, data, Definition.Actors(manager, visualizer, planner))
      api <- ApiImpl(mnId, data, Actor.spawn(definition, (b, n) => ctx.spawn(b, n)))
    yield api
