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
//package planning.engine.planner.mpi.actors.manager.logic
//
//import cats.effect.IO
//import cats.effect.unsafe.implicits.global
//import planning.engine.common.graph.edges.MeKey
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.actors.visualizer.Visualizer
//import planning.engine.planner.mpi.common.data.edge.EdgeData
//import planning.engine.planner.mpi.test.actors.ManagerTestActor
//import planning.engine.planner.mpi.test.data.MapEdgeTestData
//
//class ManageEdgesSpec extends UnitSpecWithTestKit with ManagerTestActor:
//  private class CaseData extends Case with WithManagerActor with MapEdgeTestData
//
//  "ManagerActor.UpsertEdges" should:
//    "upsert a single edge" in newCase[CaseData]: (log, data) =>
//      import data.{*, given}
//
//      val manager = managerActorTwoNode.manager
//      val meKey = MeKey.Link(managerActorTwoNode.srcMnId, managerActorTwoNode.trgMnId)
//
//      visualizerProbe.expectMessageType[Visualizer.Msg] // from managerActorTwoNode setup
//
//      val keys = log.msg(manager.upsertEdges[IO](EdgeData.Kit(Map(meKey -> edgeData1))).unsafeRunSync())
//      keys mustBe Set(meKey)
//
//      log.msg(visualizerProbe.expectMessageType[Visualizer.Msg]) // notified visualizer of the upserted edges
//      succeed
//
//    "upsert multiple edges from a single UpsertEdges message" in newCase[CaseData]: (log, data) =>
//      import data.{*, given}
//
//      val manager = managerActorTwoNode.manager
//      val srcId = managerActorTwoNode.srcMnId
//      val trgId = managerActorTwoNode.trgMnId
//      val linkKey = MeKey.Link(srcId, trgId)
//      val thenKey = MeKey.Then(srcId, trgId)
//      val edgeData = EdgeData.Kit(Map(linkKey -> edgeData1, thenKey -> edgeData2))
//
//      visualizerProbe.expectMessageType[Visualizer.Msg] // from managerActorTwoNode setup
//
//      val keys = log.msg(manager.upsertEdges[IO](edgeData).unsafeRunSync())
//      keys mustBe Set(linkKey, thenKey)
//
//      log.msg(visualizerProbe.expectMessageType[Visualizer.Msg]) // notified visualizer of the upserted edges
//      succeed
