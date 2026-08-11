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

package planning.engine.planner.mpi.actors.manager.logic

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.adaptor.Adaptor
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.actors.ManagerTestActor

class ManageNodesSpec extends UnitSpecWithTestKit with ManagerTestActor:
  private class CaseData extends Case with WithManagerActor

  "ManagerActorSpec.AddNodes" should:
    "add new nodes" in newCase[CaseData]: (log, data) =>
      import data.*

      def sendAddNodes(data: NodeData.Kit, manager: Actor.Ref): Adaptor.NodesAdded =
        manager ! Actor.AddNodes(data, adaptorProbe.ref)

        val nodesAdded = log.msg(adaptorProbe.expectMessageType[Adaptor.NodesAdded])
        val visMsg = log.msg(visualizerProbe.expectMessageType[VisualizerActor.Structure.Nodes.Added])

        visMsg.ids mustBe nodesAdded.ids
        nodesAdded

      val conRes = sendAddNodes(NodeData(conNodeData), managerActorEmpty.manager)
      conRes.ids mustBe Map(MnId.Con(1L) -> conNodeData.name)

      val absRes = sendAddNodes(NodeData(absNodeData), managerActorEmpty.manager)
      absRes.ids mustBe Map(MnId.Abs(2L) -> absNodeData.name)

      val multiRes = sendAddNodes(NodeData(conNodeData, absNodeData), managerActorEmpty.manager)
      multiRes.ids mustBe Map(MnId.Con(3L) -> conNodeData.name, MnId.Abs(4L) -> absNodeData.name)

  "ManagerActorSpec.UpsertNodesByName" should:
    "upsert node by name" in newCase[CaseData]: (log, data) =>
      import data.*

      def sendUpsertNodesByName(data: NodeData.Kit, manager: Actor.Ref): Adaptor.NodesUpserted =
        manager ! Actor.UpsertNodesByName(data, adaptorProbe.ref)

        val nodesAdded = log.msg(adaptorProbe.expectMessageType[Adaptor.NodesUpserted])
        val visMsg = log.msg(visualizerProbe.expectMessageType[VisualizerActor.Structure.Nodes.Added])

        visMsg.ids mustBe nodesAdded.ids
        nodesAdded

      val gotRes = sendUpsertNodesByName(NodeData(conNodeData, absNodeData), managerActorOneConNode.manager)

      val expectedAddedId = MnId.Abs(2L) -> absNodeData.name
      val expectedRes = managerActorOneConNode.nodes + expectedAddedId

      gotRes.ids mustBe expectedRes

    "terminate when UpsertNodesByName finds multiple IDs for the same name" in newCase[CaseData]: (log, data) =>
      import data.*

      addNodes(NodeData(conNodeData, conNodeData), managerActorEmpty.manager)
      managerActorEmpty.manager ! Actor.UpsertNodesByName(NodeData(conNodeData), adaptorProbe.ref)

      adaptorProbe.expectTerminated(managerActorEmpty.manager)
      succeed
