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
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.plan.Depth
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.data.Definition
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.node.{NodeData, StepKey}
import planning.engine.planner.mpi.model.data.samples.Sample

private[mpi] trait Node:
  def mnId: MnId
  def data: NodeData
  def repr: String

  // Upsert source end of an edge to this node.
  def upsertEdgeSrc[F[_]: MT](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit]

  // Upsert target end of an edge to this node.
  def upsertEdgeTrg[F[_]: MT](ref: MeRef, props: Map[SampleId, Sample.Props]): F[Unit]

  // Set the total count of samples (used for calculated P(N)) for this node.
  def setSampleTotalCount[F[_]: MT](count: Long): F[Unit]

  // Move context window one step forward in plan forest:
  //  1. Propagate context extension signal in the sequence tree: For each found in plan state Active
  //     steps, send the ContextExtend message to the target node of each none
  //     empty (i.e. that have active samples) THEN outgoing edge from this node.
  //  2. Propagate context shrinking signal in the sequence tree: For each Active step,
  //     send the ContextShrank message to its source node of the THEN incoming edge (there should be only
  //     one THEN incoming edge per step).
  def contextMove[F[_]: MT](active: StepKey): F[Unit]

  // Move context boundary one step forward:
  //  1. If Planned steps exist in this node plan state then replace it with Next step.
  //  2. If no Planned steps exist, then build and add new Next step.
  //  3. Notify planner about new Next step, using `NextStep` message.
  // Notes:
  // - This signal is sent from the previous node in the sequence tree, on processing of Activation message.
  // - The sequence tree expends in all possible forward directions, not just fallowing planned paths.
  def contextExtend[F[_]: MT](prev: StepKey, next: StepKey, nextTime: IoTime): F[Unit]

  // TODO
  def contextShrink[F[_]: MT](prev: StepKey, depth: Depth): F[Unit]

  // TODO
  def pathCleanup[F[_]: MT](prev: StepKey): F[Unit]

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
    def data: NodeData.Con

    // Start the activation signal propagation from this concrete node (leaf of abstraction tree)
    // to higher abstract nodes (to the roots):
    //   1. Calculate concrete node init P(N) * U(N)
    //   2. Find Next steps and replace with Active. Or add new Active step if no Next steps found.
    //   3. For each  not empty (i.e. have active samples) outcoming LINK edge, send `ActivationAbs` message
    def activation[F[_]: MT](time: IoTime, utility: Option[Double]): F[Unit]

    lazy val repr: String = s"[${mnId.reprValue}, ${data.name.repr}, ${data.ioValue.repr}]"

  trait Abs extends Node:
    def mnId: MnId.Abs
    def data: NodeData.Abs

    // Propagate the activation signal from concrete node (leafs of abstraction tree)
    // to higher abstract node (to the roots):
    //  1. Calculate and save in plan state this node P(N) * U(N) base one received from the previous
    //     node and prior values.
    //  2. Propagate further link activation signal in the abstraction tree: Send Activation
    //     message (with the new P(N) * U(N)) further to target nodes if all outcoming LINK edges,
    //     if there is some, and they not empty (i.e. have active samples).
    //  3. For Next steps, which expected time is current time + 1, replace in this node plan state
    //     with the Active steps.
    //  4. If no upper node found (i.e. this node is the root of abstraction tree), then send collected active steps
    //     to the planner, using `ActiveSteps` message.
    // Notes:
    //  - This signal is sent from the planner actor on it receive Tick message.
    //  - The activation signal will be terminated on highest abstraction nodes, which not have outcoming LINK edges,
    //    i.e. it will propagate from bottom (from concrete nodes) to top of map network and will generate
    //    forest of active trees.
    def activation[F[_]: MT](
        time: IoTime, // Current world time, used to find Next steps that expected to happen exactly the next.
        sampleIds: Set[SampleId], // Active sample Ids, used to find active edges.
        path: ActivePath,
    ): F[Unit]

    lazy val repr: String = s"(${mnId.reprValue}, ${data.name.repr})"

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
