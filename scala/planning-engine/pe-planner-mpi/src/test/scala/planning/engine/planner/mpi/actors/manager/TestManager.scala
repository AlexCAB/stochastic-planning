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
//| created: 15-Aug-26 |||||||||||*/
//
//package planning.engine.planner.mpi.actors.manager
//
//import cats.effect.IO
//import cats.effect.unsafe.IORuntime
//import planning.engine.common.values.node.{HnIndex, HnName, MnId}
//import planning.engine.planner.mpi.actors.visualizer.{FakeVisualizer, Visualizer}
//import planning.engine.planner.mpi.common.data.node.NodeData
//import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
//import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
//import org.scalatest.matchers.must.Matchers.fail
//import planning.engine.common.values.sample.SampleId
//import planning.engine.planner.mpi.actors.TestActorBase
//import planning.engine.planner.mpi.actors.manager.logic.ApiImpl
//import planning.engine.planner.mpi.actors.manager.data.State
//import planning.engine.planner.mpi.actors.node.Node
//import planning.engine.planner.mpi.common.data.samples.Sample
//
//import java.util.concurrent.atomic.AtomicInteger
//
//final case class TestManager(api: Manager, nodes: Map[MnId, Option[HnName]], visualizer: FakeVisualizer):
//  import TestManager.*
//
//  def ref: ActorRef[Manager.Msg] = api.ref
//  def state(using testKit: ActorTestKit): State = api.state
//  def stateTyped(using ActorTestKit): ManagerState = api.stateTyped
//
//  private lazy val ids = nodes.keys.toList
//  lazy val srcMnId: MnId = ids.headOption.getOrElse(fail("No nodes available in TestManager"))
//  lazy val trgMnId: MnId = ids.drop(1).headOption.getOrElse(fail("Less than two nodes available in TestManager"))
//
//  def withNodes(data: NodeData.Kit)(using ActorSystem[?], IORuntime): TestManager =
//    val nodeIds = api.addNodes[IO](data).unsafeRunSync()
//    visualizer.probe.expectMessageType[Visualizer.Msg] // Remove ShowAddNodes from visualizer mailbox
//    TestManager(api, nodes ++ nodeIds, visualizer)
//
//object TestManager extends TestActorBase:
//  type ManagerState = (
//      Long,
//      Long,
//      Map[HnIndex, Long],
//      Map[MnId, Node],
//      Map[HnName, Set[MnId]],
//      Map[SampleId, Sample.Data],
//  )
//
//  private val nameIdCounter: AtomicInteger = AtomicInteger(1)
//
//  def spawn(name: String, visualizer: FakeVisualizer)(using testKit: ActorTestKit, rt: IORuntime): Manager = Manager
//    .spawn[IO](visualizer.api, (bh, n) => testKit.spawn(bh, s"$n-$name-${nameIdCounter.getAndIncrement()}"))
//    .unsafeRunSync()
//
//  def apply(name: String, visualizer: FakeVisualizer)(using ActorTestKit, IORuntime): TestManager = new TestManager(
//    api = spawn(name, visualizer),
//    nodes = Map.empty,
//    visualizer = visualizer,
//  )
//
//  extension (api: Manager)
//    def ref: ActorRef[Manager.Msg] = api match
//      case ApiImpl(ref) => ref
//
//    def state(using testKit: ActorTestKit): State = getActorState[State](ref)
//    
//    // Allow access to the state from outside `mpi.actors.manager` package.
//    def stateTyped(using testKit: ActorTestKit): ManagerState = Tuple.fromProductTyped(state)
