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
//| created: 05.07.2026 |||||||||||*/
//
//package planning.engine.planner.mpi.actors.node.logic
//
//import cats.effect.IO
//import cats.effect.cps.*
//import org.scalatest.Assertion
//import planning.engine.common.graph.edges.MeKey
//import planning.engine.common.values.node.MnId
//import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
//import planning.engine.planner.mpi.actors.node.{Node, TestNode, WithTestNode}
//import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
//import planning.engine.planner.mpi.test.data.MapEdgeTestData
//
//class NodeStructureSpec extends UnitSpecWithIOAndTestKit with WithTestNode:
//  private class CaseData extends Case with WithNode with MapEdgeTestData:
//    lazy val srcMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNode.api, trgNodeFake.api)
//    lazy val trgMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNode.api)
//
//    def checkNodeState(node: TestNode, expOut: Map[MnId, (Node, EdgeData)], expIn: Map[MnId, Node]): Assertion =
//      val (outgoing, incoming) = node.stateTyped
//      outgoing mustBe expOut
//      incoming mustBe expIn
//
//  "Node.addEdgeSrc" should:
//    "add source end of the edge" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        srcNode.api.addEdgeSrc[IO](srcMeRef, edgeData1).logValue(tn).await
//        trgNodeFake.expectAddEdgeTrg mustBe srcMeRef
//
//        checkNodeState(srcNode, Map(trgNodeMnId -> (trgNodeFake.api, edgeData1)), Map.empty)
//
//    "report an error to the manager when message source does not match this actor ID" in newCase[CaseData]:
//      (tn, data) =>
//        import data.*
//        async[IO]:
//          val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNodeFake.api)
//          srcNode.api.addEdgeSrc[IO](badMeRef, edgeData1).logValue(tn).await
//
//          val (source, err) = fakeManager.expectReportedError
//          source mustBe srcNode.api
//          err.getMessage must include("AddEdgeSrc message source does not match this actor ID")
//
//  "Node.addEdgeTrg" should:
//    "add target end of the edge" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        trgNode.api.addEdgeTrg[IO](trgMeRef).logValue(tn).await
//
//        fakeManager.probe.expectNoMessage()
//        checkNodeState(trgNode, Map.empty, Map(srcNodeMnId -> srcNodeFake.api))
//
//    "report an error to the manager when message target does not match this actor ID" in newCase[CaseData]:
//      (tn, data) =>
//        import data.*
//        async[IO]:
//          val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNodeFake.api)
//          trgNode.api.addEdgeTrg[IO](badMeRef).logValue(tn).await
//
//          val (source, err) = fakeManager.expectReportedError
//          source mustBe trgNode.api
//          err.getMessage must include("AddEdgeTrg message target does not match this actor ID")
