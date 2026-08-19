///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 09.06.2026 |||||||||||*/
//
//package planning.engine.planner.mpi.actors.manager.logic
//
//import cats.effect.IO
//import cats.effect.cps.*
//import org.scalatest.Assertion
//import planning.engine.common.values.node.{HnName, MnId}
//import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
//import planning.engine.planner.mpi.actors.manager.{TestManager, WithTestManager}
//import planning.engine.planner.mpi.common.data.node.NodeData
//
//class ManagerNodesSpec extends UnitSpecWithIOAndTestKit with WithTestManager:
//  private class CaseData extends Case with WithManager:
//    def checkManagerState(manager: TestManager, expNodes: Map[MnId, Option[HnName]], expNextId: Long): Assertion =
//      val state = manager.state
//
//      state.nodeRefMap.keySet mustBe expNodes.keySet
//      expNodes.foreach((id, name) => state.nodeRefMap(id).name mustBe name)
//      state.nodeNameMap mustBe expNodes.collect { case (id, Some(name)) => name -> Set(id) }
//      state.nextMnId mustBe expNextId
//
//  "ManagerActorSpec.AddNodes" should:
//    "add new nodes" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        val conRes = managerEmpty.api.addNodes[IO](NodeData(conNodeData)).logValue(tn).await
//        fakeVisualizer.expectShowNodesAdded mustBe conRes
//        conRes mustBe Map(MnId.Con(1L) -> conNodeData.name)
//
//        checkManagerState(
//          managerEmpty,
//          expNodes = Map(MnId.Con(1L) -> conNodeData.name),
//          expNextId = 2L,
//        )
//
//        val absRes = managerEmpty.api.addNodes[IO](NodeData(absNodeData)).logValue(tn).await
//        fakeVisualizer.expectShowNodesAdded mustBe absRes
//        absRes mustBe Map(MnId.Abs(2L) -> absNodeData.name)
//
//        val multiRes = managerEmpty.api.addNodes[IO](NodeData(conNodeData, absNodeData)).logValue(tn).await
//        fakeVisualizer.expectShowNodesAdded mustBe multiRes
//        multiRes mustBe Map(MnId.Con(3L) -> conNodeData.name, MnId.Abs(4L) -> absNodeData.name)
//
//  "ManagerActorSpec.UpsertNodesByName" should:
//    "upsert node by name" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        val gotRes = managerOneConNode.api
//          .upsertNodesByName[IO](NodeData(conNodeData, absNodeData)).logValue(tn).await
//
//        fakeVisualizer.expectShowNodesAdded mustBe gotRes
//
//        val expectedAddedId = MnId.Abs(2L) -> absNodeData.name
//        val expectedRes = managerOneConNode.nodes + expectedAddedId
//
//        gotRes mustBe expectedRes
//
//        checkManagerState(managerOneConNode, expNodes = expectedRes, expNextId = 3L)
//
//    "terminate when UpsertNodesByName finds multiple IDs for the same name" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        managerEmpty.withNodes(NodeData(conNodeData, conNodeData))
//
//        managerEmpty.api.upsertNodesByName[IO](NodeData(conNodeData)).logValue(tn).attempt.await
//        fakeVisualizer.probe.expectTerminated(managerEmpty.ref)
//        succeed
