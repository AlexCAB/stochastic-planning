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
//| created: 03.08.2026 |||||||||||*/
//
//package planning.engine.planner.mpi.actors.manager.logic
//
//import cats.effect.IO
//import cats.effect.unsafe.implicits.global
//import planning.engine.common.values.node.MnId
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.actors.manager.Manager
//import planning.engine.planner.mpi.actors.manager.data.Message
//
//import planning.engine.planner.mpi.actors.node.FakeNode
//import planning.engine.planner.mpi.common.data.node.NodeData
//import planning.engine.planner.mpi.test.actors.ManagerTestActor
//
//class HandleErrorSpec extends UnitSpecWithTestKit with ManagerTestActor:
//  import Message.*
//
//  private class CaseData extends Case with WithManagerActor
//
//  private def actorRefOf(manager: Manager): Actor.Ref = manager match
//    case ApiImpl(ref) => ref
//
//  "ManagerActor.doHandleNodeError" should:
//    "terminate the manager actor after a NodeActorError is received" in newCase[CaseData]: (log, data) =>
//      import data.*
//
//      val manager = managerActorEmpty.manager
//      val err = new RuntimeException("Node actor boom")
//      val fakeNode = FakeNode(MnId.Con(99L), None)
//      val sender = testKit.createTestProbe[NodesAdded]()
//
//      val nodeErrorMsg = NodeActorError(fakeNode.api, Some(AddNodes(NodeData(conNodeData), sender.ref)), err)
//
//      log.msg(actorRefOf(manager) ! nodeErrorMsg)
//
//      visualizerProbe.expectTerminated(actorRefOf(manager))
//      succeed
//
//  "ManagerActor.doHandleManagerError" should:
//    "terminate the manager actor after receive raises an error" in newCase[CaseData]: (log, data) =>
//      import data.{*, given}
//
//      val manager = managerActorEmpty.manager
//      addNodes(NodeData(conNodeData, conNodeData), manager)
//
//      log.msg(manager.upsertNodesByName[IO](NodeData(conNodeData)).attempt.unsafeRunSync())
//
//      visualizerProbe.expectTerminated(actorRefOf(manager))
//      succeed
