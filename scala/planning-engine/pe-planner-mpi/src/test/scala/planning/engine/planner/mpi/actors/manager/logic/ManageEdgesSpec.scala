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
//| created: 01.07.2026 |||||||||||*/
//
//package planning.engine.planner.mpi.actors.manager
//
//import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
//import planning.engine.common.graph.edges.MeKey
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
//import planning.engine.planner.mpi.common.data.edge.EdgeData
//import planning.engine.planner.mpi.test.actors.ManagerTestActor
//import planning.engine.planner.mpi.test.data.MapEdgeTestData
//
//class ManageEdgesSpec extends UnitSpecWithTestKit with ManagerTestActor:
//  private class CaseData extends Case with WithManagerActor with MapEdgeTestData:
//    val adaptorProbe: TestProbe[ManagerAdaptor.Msg] = testKit.createTestProbe[ManagerAdaptor.Msg]()
//
//  "ManagerActor.UpsertEdges" should:
//    "upsert a single edge" in newCase[CaseData]: (log, data) =>
//      import data.*
//
//      val manager = managerActorTwoNode.manager
//      val meKey = MeKey.Link(managerActorTwoNode.srcMnId, managerActorTwoNode.trgMnId)
//
//      manager ! ManagerActor.UpsertEdges(EdgeData.Kit(Map(meKey -> edgeData1)), adaptorProbe.ref)
//
//      val res = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgeAdded])
//      res.key mustBe meKey
//
//    "upsert multiple edges from a single UpsertEdges message" in newCase[CaseData]: (log, data) =>
//      import data.*
//
//      val manager = managerActorTwoNode.manager
//      val srcId = managerActorTwoNode.srcMnId
//      val trgId = managerActorTwoNode.trgMnId
//      val linkKey = MeKey.Link(srcId, trgId)
//      val thenKey = MeKey.Then(srcId, trgId)
//
//      manager ! ManagerActor.UpsertEdges(EdgeData.Kit(Map(linkKey -> edgeData1, thenKey -> edgeData2)), adaptorProbe.ref)
//
//      val res1 = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgeAdded])
//      val res2 = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgeAdded])
//      Set(res1.key, res2.key) mustBe Set(linkKey, thenKey)