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
| created: 13-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import cats.effect.IO
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.logic.ApiImpl
import planning.engine.planner.mpi.actors.visualizer.{FakeVisualizer, Visualizer}
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.data.MapNodeTestData

import java.util.concurrent.atomic.AtomicInteger

trait TestManager:
  self: UnitSpecWithIOAndTestKit =>

  final case class ManagerWithNodes(manager: Manager, nodes: Map[MnId, Option[HnName]]):
    private lazy val ids = nodes.keys.toList
    def srcMnId: MnId = ids.headOption.getOrElse(fail("No nodes available in ManagerWithNodes"))
    def trgMnId: MnId = ids.drop(1).headOption.getOrElse(fail("Less than two nodes available in ManagerWithNodes"))

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)
  
  trait WithManager extends MapNodeTestData:
    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer()

    def makeManagerActor(name: String): Manager = Manager
      .spawn[IO](fakeVisualizer.api, (bh, n) => testKit.spawn(bh, s"$n-$name-${nameIdCounter.getAndIncrement()}"))
      .unsafeRunSync()

    lazy val managerEmpty: ManagerWithNodes = ManagerWithNodes(makeManagerActor("managerEmpty"), Map.empty)

    lazy val managerOneConNode: ManagerWithNodes = makeManagerActor("managerOneConNode")
      .withNodes(NodeData(conNodeData))

    lazy val managerTwoNode: ManagerWithNodes = makeManagerActor("managerActorTwoNode")
      .withNodes(NodeData(conNodeData, absNodeData))

    extension (manager: Manager)
      def ref: ActorRef[Manager.Msg] = manager match
        case ApiImpl(ref) => ref
    
      def withNodes(data: NodeData.Kit): ManagerWithNodes = 
        val nodeIds = manager.addNodes[IO](data).unsafeRunSync()
        fakeVisualizer.probe.expectMessageType[Visualizer.Msg] // Remove ShowAddNodes from visualizer mailbox
        ManagerWithNodes(manager, nodeIds)
      
    