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
| created: 15-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.node

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.manager.FakeManager
import planning.engine.planner.mpi.actors.node.data.State
import planning.engine.planner.mpi.actors.node.logic.ApiImpl
import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
import planning.engine.planner.mpi.common.data.node.NodeData

import java.util.concurrent.atomic.AtomicInteger

final case class TestNode(api: Node, manager: FakeManager, visualizer: FakeVisualizer):
  import TestNode.*

  def ref: ActorRef[Node.Msg] = api.ref
  def state(using ActorTestKit, IORuntime): State = api.state
  def stateTyped(using ActorTestKit, IORuntime): NodeState = api.stateTyped

object TestNode extends TestActorBase:
  type NodeState = (
      Long,
      Map[MnId, State.EdgeData],
      Map[MnId, State.EdgeData],
      Map[SampleId, State.SampleData],
      Long,
  )
  
  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  private def spawn(bh: Behavior[Node.Msg], name: String)(using testKit: ActorTestKit): ActorRef[Node.Msg] =
    testKit.spawn(bh, s"test-node-$name-${nameIdCounter.getAndIncrement()}")

  def apply(id: MnId, data: NodeData, manager: FakeManager, visualizer: FakeVisualizer)(using
      ActorTestKit,
      IORuntime,
  ): TestNode = new TestNode(
    api = Node.spawn[IO](id, data, manager.api, visualizer.api, spawn).unsafeRunSync(),
    manager = manager,
    visualizer = visualizer,
  )

  extension (api: Node)
    def ref: ActorRef[Node.Msg] = api match
      case ApiImpl(_, _, ref) => ref

    def state(using ActorTestKit, IORuntime): State = getActorState[State]("Node", ref)

    // Allow access to the state from outside `mpi.actors.node` package.
    def stateTyped(using ActorTestKit, IORuntime): NodeState = Tuple.fromProductTyped(state)
