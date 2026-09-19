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
| created: 15.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.node

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.manager.{FakeManager, Manager}
import planning.engine.planner.mpi.actors.node.data.{Definition, State}
import planning.engine.planner.mpi.actors.node.data.state.{Plan, Struct}
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.planner.{FakePlanner, Planner}
import planning.engine.planner.mpi.actors.visualizer.{FakeVisualizer, Visualizer}
import planning.engine.planner.mpi.model.data.node.NodeData

import java.util.concurrent.atomic.AtomicInteger

final case class TestNode(
    api: Node,
    manager: FakeManager,
    visualizer: FakeVisualizer,
    planner: FakePlanner,
):
  import TestNode.*

  def ref: ActorRef[Node.Msg] = api.ref
  def state(using ActorTestKit, IORuntime): State = api.state
  def stateTyped(using ActorTestKit, IORuntime): (NodeStruct, NodePlan) = api.stateTyped

object TestNode extends TestActorBase:
  type NodeStruct = (
      Long,
      Map[MnId, Struct.EdgeData],
      Map[MnId, Struct.EdgeData],
      Map[SampleId, Struct.SampleData],
      Long,
  )

  type NodePlan = (Int, Int)

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  private def makeApi(
      id: MnId,
      data: NodeData,
      manager: Manager,
      visualizer: Option[Visualizer],
      planner: Planner,
  )(using testKit: ActorTestKit, r: IORuntime): Node =
    def spawn(b: Behavior[Node.Msg], name: String): ActorRef[Node.Msg] =
      testKit.spawn(b, s"test-node-$name-${nameIdCounter.getAndIncrement()}")

    val definition = Definition[IO](id, data, Definition.Actors(manager, visualizer, planner)).unsafeRunSync()
    ApiImpl[IO](id, data, Actor.spawn(definition, spawn)).unsafeRunSync()

  def apply(
      id: MnId,
      data: NodeData,
      manager: FakeManager,
      visualizer: FakeVisualizer,
      planner: FakePlanner,
  )(using ActorTestKit, IORuntime): TestNode = new TestNode(
    api = makeApi(id, data, manager.api, Some(visualizer.api), planner.api),
    manager = manager,
    visualizer = visualizer,
    planner = planner,
  )

  extension (api: Node)
    def ref: ActorRef[Node.Msg] = api match
      case ApiImpl.Con(_, _, _, ref) => ref
      case ApiImpl.Abs(_, _, ref)    => ref

    def state(using ActorTestKit, IORuntime): State =
      val state = getActorState[State](ref)
      logObj("Node struct", state.struct)
      logObj("Node plan", state.plan)
      state

    // Allow access to the state from outside `mpi.actors.node` package.
    def stateTyped(using ActorTestKit, IORuntime): (NodeStruct, NodePlan) =
      val st = state
      (Tuple.fromProductTyped(st.struct), Tuple.fromProductTyped(st.plan))
