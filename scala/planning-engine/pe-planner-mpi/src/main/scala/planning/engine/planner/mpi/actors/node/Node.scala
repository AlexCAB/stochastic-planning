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
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.{IoTime, IoValue}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.plan.Depth
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
  //  1. Calculate and save in plan state this node P(N) * U(N) base one received from the previous
  //     node and prior values.
  //  2. Propagate further link activation signal in the abstraction tree: Send Activation
  //     message (with the new P(N) * U(N)) further to target nodes if all outcoming LINK edges,
  //     if there is some, and they not empty (i.e. have samples).
  //  3. For Next steps, which expected time is current time + 1, replace in this node plan state with the Active steps.
  //  4. In case of no Next steps found, then add one new Active steps, which will be a root of new plan tree.
  //     If some Next steps found (at 2), do not add a new Active steps (all possible plan paths should be covered
  //     by existing steps).
  //  5. Propagate context extension signal in the sequence tree: For each found or created Active
  //     steps (in 2 or 3), send the ContextExtend message to the target node of each none
  //     empty (i.e. that have samples) THEN outgoing edge from this node.
  //  6. Propagate context shrinking signal in the sequence tree: For each Active step,
  //     send the ContextShrank message to its source node of the THEN incoming edge (there should be only
  //     one THEN incoming edge per step).
  //  7. For found but not activated Next steps (in 2), send the PathCut message to its
  //     source node of the THEN incoming edge. Then send the TreeCut message to the target node of
  //     each THEN outgoing edge from this node.
  // Notes:
  //  - This signal is sent from the planner actor on it receive Step message.
  //  - The activation signal will be terminated on highest abstraction nodes, which not have outcoming LINK edges,
  //    i.e. it will propagate from bottom (from concrete nodes) to top of map network and will generate
  //    forest of active trees.
  //  - The sequence tree expends in all possible forward directions, not just fallowing planned paths.
  //  - 1, 2, 3, 4, 5 is the activation signal propagation part.
  //  - 6, 7 is the context cleanup part.
  def activation[F[_]: MT](
      bottom: StepKey, // The bottom node step key (node where the activation signal was generated).
      up: StepKey, // The upper node step key (node where the activation signal is processed).
      time: IoTime, // Current world time, used to find Next steps that expected to happen exactly the next.
      pu: PU, // P(N) * U(N), Probability and utility of the `bottom` node
      sampleIds: Set[SampleId], // Active sample Ids, used to find active edges.
  ): F[Unit]

  // Move context boundary one step forward:
  //  1. If Planned steps exist in this node plan state then replace it with Next step.
  //  2. If no Planned steps exist, then build and add new Next step.
  // Notes:
  // - This signal is sent from the previous node in the sequence tree, on processing of Activation message.
  def contextExtend[F[_]: MT](prev: StepKey, next: StepKey, nextTime: IoTime): F[Unit]

  // TODO
  def contextShrink[F[_]: MT](prev: StepKey, depth: Depth): F[Unit]

  // TODO
  def pathCut[F[_]: MT](prev: StepKey): F[Unit]

  // TODO
  def treeCut[F[_]: MT](next: StepKey): F[Unit]

  // TODO
  def inference[F[_]: MT](prev: StepKey, next: StepKey): F[Unit]

  // TODO
  def planExtend[F[_]: MT](prev: StepKey, next: StepKey): F[Unit]

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
