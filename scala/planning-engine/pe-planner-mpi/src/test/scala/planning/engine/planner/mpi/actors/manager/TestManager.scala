/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 15-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.visualizer.{FakeVisualizer, Visualizer}
import planning.engine.planner.mpi.common.data.node.NodeData
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
import org.scalatest.matchers.must.Matchers.fail
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.manager.logic.ApiImpl
import planning.engine.planner.mpi.actors.manager.data.State
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.samples.Sample

import java.util.concurrent.atomic.AtomicInteger

final case class TestManager(
    api: Manager,
    nodes: Map[MnId, NodeData],
    samples: Map[SampleId, (Sample.Props, Option[Sample.Info])],
    visualizer: FakeVisualizer,
):
  import TestManager.*

  def ref: ActorRef[Manager.Msg] = api.ref
  def state(using ActorTestKit, IORuntime): State = api.state
  def stateTyped(using ActorTestKit, IORuntime): ManagerState = api.stateTyped

  private lazy val ids = nodes.keys.toList
  lazy val srcMnId: MnId = ids.headOption.getOrElse(fail("No nodes available in TestManager"))
  lazy val trgMnId: MnId = ids.drop(1).headOption.getOrElse(fail("Less than two nodes available in TestManager"))
  lazy val firstSampleId: SampleId = samples.keys.headOption.getOrElse(fail("No samples available in TestManager"))

  // Add a single node and return a new TestManager with the updated state.
  // Note: It will spawn a real Node actor, not the fake one.
  def withNode(data: NodeData)(using ActorSystem[?], IORuntime): TestManager =
    val mnId = api.addNode[IO](data).unsafeRunSync()
    visualizer.probe.expectMessageType[Visualizer.Msg] // Remove ShowAddNodes from visualizer mailbox
    TestManager(api, nodes ++ Map(mnId -> data), samples, visualizer)

  def withNodes(data: NodeData*)(using ActorSystem[?], IORuntime): TestManager =
    data.foldLeft(this)((tm, nd) => tm.withNode(nd))

  // Add a single sample and return a new TestManager with the updated state.
  // Note: It's adding only sample data, no new nodes and no edges will be added.
  def withSample(props: Sample.Props, info: Sample.Info)(using ActorSystem[?], IORuntime): TestManager =
    val sampleMap = api.addManSamples[IO](Set(Sample.Man(props, info, Set.empty)), Map.empty).unsafeRunSync()
    TestManager(api, nodes, samples ++ sampleMap.view.mapValues(d => (d.props, Some(d.info))).toMap, visualizer)

  def withSample(props: Sample.Props, name: String = "test-sample")(using ActorSystem[?], IORuntime): TestManager =
    withSample(props, Sample.Info(Name(name), None))

object TestManager extends TestActorBase:
  type ManagerState = (
      Long,
      Long,
      Map[MnId, Node],
      Map[HnName, Set[MnId]],
      Map[SampleId, (Sample.Props, Option[Sample.Info])],
  )

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  def spawn(name: String, visualizer: FakeVisualizer)(using testKit: ActorTestKit, rt: IORuntime): Manager = Manager
    .spawn[IO](visualizer.api, (bh, n) => testKit.spawn(bh, s"$n-$name-${nameIdCounter.getAndIncrement()}"))
    .unsafeRunSync()

  def apply(name: String, visualizer: FakeVisualizer)(using ActorTestKit, IORuntime): TestManager = new TestManager(
    api = spawn(name, visualizer),
    nodes = Map.empty,
    samples = Map.empty,
    visualizer = visualizer,
  )

  extension (api: Manager)
    def ref: ActorRef[Manager.Msg] = api match
      case ApiImpl(ref) => ref

    def state(using ActorTestKit, IORuntime): State = getActorState[State]("Manager", ref)

    // Allow access to the state from outside `mpi.actors.manager` package.
    def stateTyped(using ActorTestKit, IORuntime): ManagerState = (
      state.nextMnId,
      state.nextSampleId,
      state.nodeRefMap,
      state.nodeNameMap,
      state.sampleDataMap.view.mapValues(d => (d.props, d.info)).toMap,
    )
