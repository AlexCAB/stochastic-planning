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
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.data.node.NodeData
import planning.engine.planner.mpi.test.actors.ManagerTestActor

class ManagerActorSpec extends UnitSpecWithTestKit with ManagerTestActor:
  private class CaseData extends Case with WithManagerActor:
    val adaptorProbe: TestProbe[ManagerAdaptor.Msg] = testKit.createTestProbe[ManagerAdaptor.Msg]()

  "ManagerActorSpec.AddNodes" should:
    "add new nodes" in newCase[CaseData]: (log, data) =>
      import data.*

      def sendAddNodes(data: NodeData.Kit, manager: ManagerActor.Ref): ManagerAdaptor.NodesAdded =
        manager ! ManagerActor.AddNodes(data, adaptorProbe.ref)
        adaptorProbe.expectMessageType[ManagerAdaptor.NodesAdded]

      val conRes = log.msg(sendAddNodes(NodeData(conNodeData), managerActorEmpty.manager))
      conRes.ids mustBe Map(MnId.Con(1L) -> conNodeData.name)

      val absRes = log.msg(sendAddNodes(NodeData(absNodeData), managerActorEmpty.manager))
      absRes.ids mustBe Map(MnId.Abs(2L) -> absNodeData.name)

      val multiRes = log.msg(sendAddNodes(NodeData(conNodeData, absNodeData), managerActorEmpty.manager))
      multiRes.ids mustBe Map(MnId.Con(3L) -> conNodeData.name, MnId.Abs(4L) -> absNodeData.name)

  "ManagerActorSpec.UpsertNodesByName" should:
    "upsert node by name" in newCase[CaseData]: (log, data) =>
      import data.*

      def sendUpsertNodesByName(data: NodeData.Kit, manager: ManagerActor.Ref): ManagerAdaptor.NodesAdded =
        manager ! ManagerActor.UpsertNodesByName(data, adaptorProbe.ref)
        adaptorProbe.expectMessageType[ManagerAdaptor.NodesAdded]

      val gotRes = log.msg(sendUpsertNodesByName(NodeData(conNodeData, absNodeData), managerActorOneConNode.manager))
      val expectedRes = managerActorOneConNode.nodes + (MnId.Abs(2L) -> absNodeData.name)

      gotRes.ids mustBe expectedRes

    "return NodesError when UpsertNodesByName finds multiple IDs for the same name" in newCase[CaseData]: (log, data) =>
      import data.*

      addNodes(NodeData(conNodeData, conNodeData), managerActorEmpty.manager)

      managerActorEmpty.manager ! ManagerActor.UpsertNodesByName(NodeData(conNodeData), adaptorProbe.ref)
      val errorRes = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.NodesError])
      errorRes.err.getMessage must include("Expected exactly one node ID for name")
      errorRes.ids mustBe Set(MnId.Con(1L), MnId.Con(2L))
