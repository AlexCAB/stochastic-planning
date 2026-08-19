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
//import cats.effect.cps.*
//import planning.engine.common.values.node.MnId
//import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
//import planning.engine.planner.mpi.actors.manager.data.Message.{AddNodes, NodesAdded}
//import planning.engine.planner.mpi.actors.manager.WithTestManager
//import planning.engine.planner.mpi.actors.node.FakeNode
//import planning.engine.planner.mpi.common.data.node.NodeData
//
//class ManagerErrorsSpec extends UnitSpecWithIOAndTestKit with WithTestManager:
//  private class CaseData extends Case with WithManager
//
//  "ManagerActor.doHandleNodeError" should:
//    "terminate the manager actor after a NodeActorError is received" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        val err = new RuntimeException("Node actor boom")
//        val fakeNode = FakeNode(MnId.Con(99L), None)
//        val sender = testKit.createTestProbe[NodesAdded]("test-sender")
//
//        managerEmpty.api.reportError[IO](fakeNode.api, Some(AddNodes(NodeData(conNodeData), sender.ref)), err).await
//
//        fakeVisualizer.probe.expectTerminated(managerEmpty.ref)
//        succeed
//
//  "ManagerActor.doHandleManagerError" should:
//    "terminate the manager actor after receive raises an error" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        managerEmpty.withNodes(NodeData(conNodeData, conNodeData))
//
//        managerEmpty.api.upsertNodesByName[IO](NodeData(conNodeData)).logValue(tn).attempt.await
//
//        fakeVisualizer.probe.expectTerminated(managerEmpty.ref)
//        succeed
