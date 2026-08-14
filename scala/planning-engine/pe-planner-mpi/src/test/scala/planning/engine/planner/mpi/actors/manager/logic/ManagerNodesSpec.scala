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

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.TestManager
import planning.engine.planner.mpi.common.data.node.NodeData

class ManagerNodesSpec extends UnitSpecWithIOAndTestKit with TestManager:
  private class CaseData extends Case with WithManager

  "ManagerActorSpec.AddNodes" should:
    "add new nodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val manager = managerEmpty.manager

        val conRes = manager.addNodes[IO](NodeData(conNodeData)).logValue(tn).await
        fakeVisualizer.expectShowNodesAdded mustBe conRes
        conRes mustBe Map(MnId.Con(1L) -> conNodeData.name)

        val absRes = manager.addNodes[IO](NodeData(absNodeData)).logValue(tn).await
        fakeVisualizer.expectShowNodesAdded mustBe absRes
        absRes mustBe Map(MnId.Abs(2L) -> absNodeData.name)

        val multiRes = manager.addNodes[IO](NodeData(conNodeData, absNodeData)).logValue(tn).await
        fakeVisualizer.expectShowNodesAdded mustBe multiRes
        multiRes mustBe Map(MnId.Con(3L) -> conNodeData.name, MnId.Abs(4L) -> absNodeData.name)

  "ManagerActorSpec.UpsertNodesByName" should:
    "upsert node by name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val gotRes = managerOneConNode.manager.upsertNodesByName[IO](NodeData(conNodeData, absNodeData)).logValue(tn).await
        fakeVisualizer.expectShowNodesAdded mustBe gotRes

        val expectedAddedId = MnId.Abs(2L) -> absNodeData.name
        val expectedRes = managerOneConNode.nodes + expectedAddedId

        gotRes mustBe expectedRes

    "terminate when UpsertNodesByName finds multiple IDs for the same name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val manager = managerEmpty.manager
        manager.withNodes(NodeData(conNodeData, conNodeData))

        manager.upsertNodesByName[IO](NodeData(conNodeData)).logValue(tn).attempt.await

        fakeVisualizer.probe.expectTerminated(manager.ref)
        succeed
