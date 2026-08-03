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
//package planning.engine.planner.mpi.actors.node
//
//import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
//import planning.engine.common.graph.edges.MeKey
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
//import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor.Msg
//import planning.engine.planner.mpi.common.data.edge.MeRef
//import planning.engine.planner.mpi.test.actors.NodeTestActor
//import planning.engine.planner.mpi.test.data.MapEdgeTestData
//
//class NodeStructureSpec extends UnitSpecWithTestKit with NodeTestActor:
//  private class CaseData extends Case with WithNodeActor with MapEdgeTestData:
//    lazy val adaptorProbe: TestProbe[Msg] = testKit.createTestProbe[ManagerAdaptor.Msg]("adaptor-probe")
//    lazy val trgNodeProbe: TestProbe[NodeActor.Msg] = testKit.createTestProbe[NodeActor.Msg]("trg-node-probe")
//    lazy val srcNodeProbe: TestProbe[NodeActor.Msg] = testKit.createTestProbe[NodeActor.Msg]("src-node-probe")
//
//    lazy val srcMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNode, trgNodeProbe.ref)
//    lazy val trgMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeProbe.ref, trgNode)
//
//  "NodeActor.AddEdgeSrc" should:
//    "add source end of the edge" in newCase[CaseData]: (log, data) =>
//      import data.*, NodeActor.*
//
//      srcNode ! AddEdgeSrc(srcMeRef, edgeData1, adaptorProbe.ref)
//
//      val trgMsg = log.msg(trgNodeProbe.expectMessageType[AddEdgeTrg])
//      trgMsg.ref mustBe srcMeRef
//      trgMsg.replyTo mustBe adaptorProbe.ref
//
//    "fail with EdgeError when message source does not match this actor ID" in newCase[CaseData]: (log, data) =>
//      import data.*, NodeActor.*
//
//      val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeProbe.ref, trgNodeProbe.ref)
//      srcNode ! AddEdgeSrc(badMeRef, edgeData1, adaptorProbe.ref)
//
//      val errorMsg = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgesError])
//      errorMsg.keys mustBe Set(badMeRef.key)
//      errorMsg.err.getMessage must include("AddEdgeSrc message source does not match this actor ID")
//      
//  "NodeActor.AddEdgeTrg" should:
//    "add target end of the edge and reply EdgeAdded" in newCase[CaseData]: (log, data) =>
//      import data.*, NodeActor.*
//
//      trgNode ! AddEdgeTrg(trgMeRef, adaptorProbe.ref)
//
//      val addedMsg = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgeAdded])
//      addedMsg.key mustBe trgMeRef.key
//
//    "fail with EdgeError when message target does not match this actor ID" in newCase[CaseData]: (log, data) =>
//      import data.*, NodeActor.*
//
//      val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeProbe.ref, trgNodeProbe.ref)
//      trgNode ! AddEdgeTrg(badMeRef, adaptorProbe.ref)
//
//      val errorMsg = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgesError])
//      errorMsg.keys mustBe Set(badMeRef.key)
//      errorMsg.err.getMessage must include("AddEdgeTrg message target does not match this actor ID")
//
//
