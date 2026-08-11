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
//package planning.engine.planner.mpi.test.actors
//
//import cats.effect.IO
//import cats.effect.unsafe.implicits.global
//import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
//import org.apache.pekko.actor.typed.ActorSystem
//import planning.engine.common.values.node.{HnName, MnId}
//import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
//import planning.engine.planner.mpi.actors.manager.Manager
//import planning.engine.planner.mpi.actors.visualizer.{FakeVisualizer, Visualizer}
//import planning.engine.planner.mpi.common.data.node.NodeData
//import planning.engine.planner.mpi.test.data.MapNodeTestData
//
//import java.util.concurrent.atomic.AtomicInteger
//
//trait ManagerTestActor:
//  self: UnitSpecWithTestKit =>
//
//  final case class ManagerWithNodes(manager: Manager, nodes: Map[MnId, Option[HnName]]):
//    private lazy val ids = nodes.keys.toList
//    def srcMnId: MnId = ids.headOption.getOrElse(fail("No nodes available in ManagerWithNodes"))
//    def trgMnId: MnId = ids.drop(1).headOption.getOrElse(fail("Less than two nodes available in ManagerWithNodes"))
//
//  private val nameIdCounter: AtomicInteger = AtomicInteger(1)
//
//  trait WithManagerActor extends MapNodeTestData:
//    given system: ActorSystem[Nothing] = testKit.system
//
//    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer
//    lazy val visualizerProbe: TestProbe[Visualizer.Msg] = fakeVisualizer.probe
//
//    def makeManagerActor(name: String): Manager = Manager
//      .spawn[IO](fakeVisualizer.api, (bh, n) => testKit.spawn(bh, s"$n-$name-${nameIdCounter.getAndIncrement()}"))
//      .unsafeRunSync()
//
//    def addNodes(data: NodeData.Kit, manager: Manager): ManagerWithNodes =
//      ManagerWithNodes(manager, manager.addNodes[IO](data).unsafeRunSync())
//
//    lazy val managerActorEmpty: ManagerWithNodes = ManagerWithNodes(makeManagerActor("managerActorEmpty"), Map.empty)
//
//    lazy val managerActorOneConNode: ManagerWithNodes =
//      addNodes(NodeData(conNodeData), makeManagerActor("managerActorOneConNode"))
//
//    lazy val managerActorTwoNode: ManagerWithNodes =
//      addNodes(NodeData(conNodeData, absNodeData), makeManagerActor("managerActorTwoNode"))
