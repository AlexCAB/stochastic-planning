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
| created: 01.07.2026 |||||||||||*/

package planning.engine.planner.mpi.test.actors

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.data.node.NodeData
import planning.engine.planner.mpi.test.data.MapNodeTestData

import java.util.concurrent.atomic.AtomicInteger

trait ManagerTestActor:
  self: UnitSpecWithTestKit =>

  final case class ManagerWithNodes(manager: ManagerActor.Ref, nodes: Map[MnId, Option[HnName]])

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  trait WithManagerActor extends MapNodeTestData:
    lazy val visualizerProbe: TestProbe[VisualizerActor.Msg] = testKit.createTestProbe[VisualizerActor.Msg]()

    def makeManagerActor(name: String): ManagerActor.Ref = ManagerActor
      .spawn(
        ManagerActor.Definition(visualizerProbe.ref),
        (bh, n) => testKit.spawn(bh, s"$n-$name-${nameIdCounter.getAndIncrement()}"),
      )

    def addNodes(data: NodeData.Kit, manager: ManagerActor.Ref): ManagerWithNodes =
      val adaptor = testKit.createTestProbe[ManagerAdaptor.Msg]("add-nodes-probe")
      manager ! ManagerActor.AddNodes(data, adaptor.ref)
      val res = adaptor.expectMessageType[ManagerAdaptor.NodesAdded]
      adaptor.stop()
      ManagerWithNodes(manager, res.ids)

    lazy val managerActorEmpty: ManagerWithNodes = ManagerWithNodes(makeManagerActor("managerActorEmpty"), Map.empty)

    lazy val managerActorOneConNode: ManagerWithNodes =
      addNodes(NodeData(conNodeData), makeManagerActor("managerActorOneConNode"))

    lazy val managerActorTwoNode: ManagerWithNodes =
      addNodes(NodeData(conNodeData, absNodeData), makeManagerActor("managerActorTwoNode"))
