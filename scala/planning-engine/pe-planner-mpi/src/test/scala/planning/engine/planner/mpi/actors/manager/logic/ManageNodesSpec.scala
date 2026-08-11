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
//import cats.effect.unsafe.implicits.global
//import planning.engine.common.values.node.{HnName, MnId}
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.actors.manager.Manager
//import planning.engine.planner.mpi.actors.visualizer.Visualizer
//import planning.engine.planner.mpi.common.data.node.NodeData
//import planning.engine.planner.mpi.test.actors.ManagerTestActor
//
//class ManageNodesSpec extends UnitSpecWithTestKit with ManagerTestActor:
//  private class CaseData extends Case with WithManagerActor
//
//  private def actorRefOf(manager: Manager): Actor.Ref = manager match
//    case ApiImpl(ref) => ref
//
//  "ManagerActorSpec.AddNodes" should:
//    "add new nodes" in newCase[CaseData]: (log, data) =>
//      import data.{*, given}
//
//      def sendAddNodes(nodeData: NodeData.Kit, manager: Manager): Map[MnId, Option[HnName]] =
//        val ids = log.msg(manager.addNodes[IO](nodeData).unsafeRunSync())
//        log.msg(visualizerProbe.expectMessageType[Visualizer.Msg]) // notified visualizer of the added nodes
//        ids
//
//      val conRes = sendAddNodes(NodeData(conNodeData), managerActorEmpty.manager)
//      conRes mustBe Map(MnId.Con(1L) -> conNodeData.name)
//
//      val absRes = sendAddNodes(NodeData(absNodeData), managerActorEmpty.manager)
//      absRes mustBe Map(MnId.Abs(2L) -> absNodeData.name)
//
//      val multiRes = sendAddNodes(NodeData(conNodeData, absNodeData), managerActorEmpty.manager)
//      multiRes mustBe Map(MnId.Con(3L) -> conNodeData.name, MnId.Abs(4L) -> absNodeData.name)
//
//  "ManagerActorSpec.UpsertNodesByName" should:
//    "upsert node by name" in newCase[CaseData]: (log, data) =>
//      import data.{*, given}
//
//      val initial = managerActorOneConNode
//      log.msg(visualizerProbe.expectMessageType[Visualizer.Msg]) // from managerActorOneConNode setup
//
//      def sendUpsertNodesByName(nodeData: NodeData.Kit, manager: Manager): Map[MnId, Option[HnName]] =
//        val ids = log.msg(manager.upsertNodesByName[IO](nodeData).unsafeRunSync())
//        log.msg(visualizerProbe.expectMessageType[Visualizer.Msg]) // notified visualizer of the upserted nodes
//        ids
//
//      val gotRes = sendUpsertNodesByName(NodeData(conNodeData, absNodeData), initial.manager)
//
//      val expectedAddedId = MnId.Abs(2L) -> absNodeData.name
//      val expectedRes = initial.nodes + expectedAddedId
//
//      gotRes mustBe expectedRes
//
//    "terminate when UpsertNodesByName finds multiple IDs for the same name" in newCase[CaseData]: (log, data) =>
//      import data.{*, given}
//
//      val manager = managerActorEmpty.manager
//      addNodes(NodeData(conNodeData, conNodeData), manager)
//
//      manager.upsertNodesByName[IO](NodeData(conNodeData)).attempt.unsafeRunSync()
//
//      visualizerProbe.expectTerminated(actorRefOf(manager))
//      succeed
