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
| created: 09.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.data.node.NodeData
import planning.engine.planner.mpi.test.data.MapNodeTestData

class ManagerActorSpec extends UnitSpecWithTestKit:
  private class CaseData extends Case with MapNodeTestData:
    val visualizerProbe: TestProbe[VisualizerActor.Msg] = testKit.createTestProbe[VisualizerActor.Msg]()
    val adaptorProbe: TestProbe[ManagerAdaptor.Msg] = testKit.createTestProbe[ManagerAdaptor.Msg]()

    lazy val mapManagerActor: ManagerActor.Ref = ManagerActor
      .spawn(ManagerActor.Definition(visualizerProbe.ref), (bh, n) => testKit.spawn(bh, n))

  "ManagerNodesLogicSpec" should:
    "add new nodes" in newCase[CaseData]: (log, data) =>
      data.mapManagerActor ! ManagerActor.AddNodes(NodeData(data.conNodeData), data.adaptorProbe.ref)

      val conRes = log.msg(data.adaptorProbe.expectMessageType[ManagerAdaptor.NodesAdded])
      conRes.ids mustBe Map(MnId.Con(1L) -> data.conNodeData.name)

      data.mapManagerActor ! ManagerActor.AddNodes(NodeData(data.absNodeData), data.adaptorProbe.ref)

      val absRes = log.msg(data.adaptorProbe.expectMessageType[ManagerAdaptor.NodesAdded])
      absRes.ids mustBe Map(MnId.Abs(2L) -> data.absNodeData.name)

      data.mapManagerActor ! ManagerActor.AddNodes(NodeData(data.conNodeData, data.absNodeData), data.adaptorProbe.ref)

      val multiRes = log.msg(data.adaptorProbe.expectMessageType[ManagerAdaptor.NodesAdded])
      multiRes.ids mustBe Map(MnId.Con(3L) -> data.conNodeData.name, MnId.Abs(4L) -> data.absNodeData.name)


