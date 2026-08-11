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
//package planning.engine.planner.mpi.test.actors
//
//import cats.effect.IO
//import cats.effect.unsafe.implicits.global
//import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
//import planning.engine.common.values.node.MnId
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.actors.manager.{FakeManager, Manager}
//import planning.engine.planner.mpi.actors.node.Node
//import planning.engine.planner.mpi.actors.visualizer.{FakeVisualizer, Visualizer}
//import planning.engine.planner.mpi.common.data.node.NodeData
//import planning.engine.planner.mpi.test.data.MapNodeTestData
//
//import java.util.concurrent.atomic.AtomicInteger
//
//trait NodeTestActor:
//  self: UnitSpecWithTestKit =>
//
//  private val nameIdCounter: AtomicInteger = AtomicInteger(1)
//
//  trait WithNodeActor extends MapNodeTestData:
//    lazy val fakeManager: FakeManager = FakeManager
//    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer
//
//    lazy val managerProbe: TestProbe[Manager.Msg] = fakeManager.probe
//    lazy val visualizerProbe: TestProbe[Visualizer.Msg] = fakeVisualizer.probe
//
//    def makeNodeActor(id: MnId, data: NodeData): Node = Node
//      .spawn[IO](
//        id,
//        data,
//        fakeManager.api,
//        fakeVisualizer.api,
//        (bh, name) => testKit.spawn(bh, s"test-node-$name-${nameIdCounter.getAndIncrement()}"),
//      )
//      .unsafeRunSync()
//
//    lazy val srcNodeMnId: MnId.Con = MnId.Con(1L)
//    lazy val trgNodeMnId: MnId.Abs = MnId.Abs(2L)
//
//    lazy val srcNode: Node = makeNodeActor(srcNodeMnId, conNodeData)
//    lazy val trgNode: Node = makeNodeActor(trgNodeMnId, absNodeData)
