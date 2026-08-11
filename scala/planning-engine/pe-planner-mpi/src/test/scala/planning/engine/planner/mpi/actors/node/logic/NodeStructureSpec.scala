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
//import cats.effect.unsafe.implicits.global
//import planning.engine.common.graph.edges.MeKey
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.actors.manager.Manager
//import planning.engine.planner.mpi.actors.node.data.Message.AddEdgeTrg
//import planning.engine.planner.mpi.actors.node.FakeNode
//import planning.engine.planner.mpi.common.data.edge.MeRef
//import planning.engine.planner.mpi.test.actors.NodeTestActor
//import planning.engine.planner.mpi.test.data.MapEdgeTestData
//
//class NodeStructureSpec extends UnitSpecWithTestKit with NodeTestActor:
//  private class CaseData extends Case with WithNodeActor with MapEdgeTestData:
//    lazy val trgNodeFake: FakeNode = FakeNode(trgNodeMnId, absNodeData.name)
//    lazy val srcNodeFake: FakeNode = FakeNode(srcNodeMnId, conNodeData.name)
//
//    lazy val srcMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNode, trgNodeFake.api)
//    lazy val trgMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNode)
//
//  private def renderedOf(msg: Manager.Msg): String = msg.longAutoRepr[IO].unsafeRunSync().mkString("\n")
//
//  "Node.addEdgeSrc" should:
//    "add source end of the edge" in newCase[CaseData]: (log, data) =>
//      import data.*
//
//      srcNode.addEdgeSrc[IO](srcMeRef, edgeData1).unsafeRunSync()
//
//      val trgMsg = log.msg(trgNodeFake.probe.expectMessageType[AddEdgeTrg])
//      trgMsg.ref mustBe srcMeRef
//
//    "report an error to the manager when message source does not match this actor ID" in newCase[CaseData]:
//      (log, data) =>
//        import data.*
//
//        val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNodeFake.api)
//        srcNode.addEdgeSrc[IO](badMeRef, edgeData1).unsafeRunSync()
//
//        val errorMsg = log.msg(managerProbe.expectMessageType[Manager.Msg])
//        renderedOf(errorMsg) must include("AddEdgeSrc message source does not match this actor ID")
//
//  "Node.addEdgeTrg" should:
//    "add target end of the edge without reporting an error" in newCase[CaseData]: (log, data) =>
//      import data.*
//
//      log.msg(trgNode.addEdgeTrg[IO](trgMeRef).unsafeRunSync())
//
//      managerProbe.expectNoMessage()
//      succeed
//
//    "report an error to the manager when message target does not match this actor ID" in newCase[CaseData]:
//      (log, data) =>
//        import data.*
//
//        val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNodeFake.api)
//        trgNode.addEdgeTrg[IO](badMeRef).unsafeRunSync()
//
//        val errorMsg = log.msg(managerProbe.expectMessageType[Manager.Msg])
//        renderedOf(errorMsg) must include("AddEdgeTrg message target does not match this actor ID")
