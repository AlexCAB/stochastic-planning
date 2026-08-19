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
//| created: 13-Aug-26 |||||||||||*/
//
//package planning.engine.planner.mpi.actors.manager
//
//import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
//import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
//import planning.engine.planner.mpi.common.data.node.NodeData
//import planning.engine.planner.mpi.test.data.MapNodeTestData
//
//trait WithTestManager:
//  self: UnitSpecWithIOAndTestKit =>
//  
//  trait WithManager extends MapNodeTestData:
//    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer()
//    
//    lazy val managerEmpty: TestManager = TestManager("managerEmpty", fakeVisualizer)
//
//    lazy val managerOneConNode: TestManager = TestManager("managerOneConNode", fakeVisualizer)
//      .withNodes(NodeData(conNodeData))
//
//    lazy val managerTwoNode: TestManager = TestManager("managerActorTwoNode", fakeVisualizer)
//      .withNodes(NodeData(conNodeData, absNodeData))
