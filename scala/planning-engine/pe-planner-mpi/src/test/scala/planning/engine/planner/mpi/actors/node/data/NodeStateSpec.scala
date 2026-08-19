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
| created: 04.07.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.effect.IO
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.{TestActorBase, UnitSpecWithIOAndTestKit}
import planning.engine.planner.mpi.actors.manager.FakeManager
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.node.{FakeNode, Node}
import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
import planning.engine.planner.mpi.common.data.edge.MeRef
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.test.data.MapNodeTestData

import java.util.concurrent.atomic.AtomicInteger

// State.upsertEdgeSrc/upsertEdgeTrg require a live Actor.Def/Actor.Ctx (to resolve `self`), so they can only
// be exercised through a real, spawned Node actor rather than by calling State methods directly.
class NodeStateSpec extends UnitSpecWithIOAndTestKit with TestActorBase:

  TODO: Refacore


  private val nameCounter = new AtomicInteger(1)

  private class CaseData extends Case with MapNodeTestData:
    val srcMnId: MnId.Con = MnId.Con(1)
    val trgMnId: MnId.Abs = MnId.Abs(2)
    val linkKey: MeKey = MeKey.Link(srcMnId, trgMnId)

    val fakeManager: FakeManager = FakeManager()
    val fakeVisualizer: FakeVisualizer = FakeVisualizer()

    private def spawnNode(id: MnId, data: NodeData): Node = Node
      .spawn[IO](id, data, fakeManager.api, fakeVisualizer.api,
        (bh, n) => testKit.spawn(bh, s"$n-${nameCounter.getAndIncrement()}"))
      .unsafeRunSync()

    val srcNode: Node = spawnNode(srcMnId, conNodeData)
    val trgNode: Node = spawnNode(trgMnId, absNodeData)

    val srcFake: FakeNode = FakeNode(srcMnId, conNodeData.name)
    val trgFake: FakeNode = FakeNode(trgMnId, absNodeData.name)

    val meRefSrc: MeRef = MeRef(linkKey, srcNode, trgFake.api) // Real srcNode, fake target, for upsertEdgeSrc
    val meRefTrg: MeRef = MeRef(linkKey, srcFake.api, trgNode) // Fake source, real trgNode, for upsertEdgeTrg

    val props1: Map[SampleId, Sample.Props] = Map(
      SampleId(1) -> Sample.Props(1L, 1.0),
      SampleId(2) -> Sample.Props(1L, 1.0),
      SampleId(3) -> Sample.Props(1L, 1.0),
    )

    val props2: Map[SampleId, Sample.Props] = Map(
      SampleId(4) -> Sample.Props(1L, 1.0),
      SampleId(5) -> Sample.Props(1L, 1.0),
    )

    private def actorRef(node: Node): ActorRef[Actor.Msg] = node match
      case ApiImpl(_, _, ref) => ref

    def srcState: State = getActorState[State](actorRef(srcNode))
    def trgState: State = getActorState[State](actorRef(trgNode))

  "State.upsertEdgeSrc(...)" should:
    "add edge to outgoing map and sample map when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      srcNode.upsertEdgeSrc[IO](meRefSrc, props1)
        .asserting: _ =>
          trgFake.expectUpsertEdgeTrg mustBe (meRefSrc, props1)
          srcState.outgoingMap mustBe Map(trgMnId -> State.EdgeData(trgFake.api, props1.keySet))
          srcState.sampleMap.keySet mustBe props1.keySet
          srcState.nextHnIndex mustBe 4L

    "join sample IDs when edge to same target already exists" in newCase[CaseData]: (_, data) =>
      import data.*
      (for
        _ <- srcNode.upsertEdgeSrc[IO](meRefSrc, props1)
        _ = trgFake.expectUpsertEdgeTrg
        _ <- srcNode.upsertEdgeSrc[IO](meRefSrc, props2)
      yield ())
        .asserting: _ =>
          trgFake.expectUpsertEdgeTrg mustBe (meRefSrc, props2)
          val allSampleIds = props1.keySet ++ props2.keySet
          srcState.outgoingMap mustBe Map(trgMnId -> State.EdgeData(trgFake.api, allSampleIds))
          srcState.sampleMap.keySet mustBe allSampleIds
          srcState.nextHnIndex mustBe 6L

    "report an error to the manager when edge source does not match this actor" in newCase[CaseData]: (_, data) =>
      import data.*
      val badMeRef = MeRef(linkKey, trgFake.api, trgFake.api) // srcNode field should be srcNode, not trgFake
      srcNode.upsertEdgeSrc[IO](badMeRef, props1)
        .asserting: _ =>
          val (source, err) = fakeManager.expectReportedError
          source mustBe srcNode
          err.getMessage must include("Edge source node does not match this node")

  "State.upsertEdgeTrg(...)" should:
    "add edge to incoming map and sample map when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      trgNode.upsertEdgeTrg[IO](meRefTrg, props1)
        .asserting: _ =>
          trgState.incomingMap mustBe Map(srcMnId -> State.EdgeData(srcFake.api, props1.keySet))
          trgState.sampleMap.keySet mustBe props1.keySet
          trgState.nextHnIndex mustBe 4L

    "leave state unchanged when the same edge and samples are upserted again" in newCase[CaseData]: (_, data) =>
      import data.*
      for
        _ <- trgNode.upsertEdgeTrg[IO](meRefTrg, props1)
        filled <- IO.delay(trgState)
        _ <- trgNode.upsertEdgeTrg[IO](meRefTrg, props1)
        result <- IO.delay(trgState)
      yield result mustBe filled

    "report an error to the manager when edge target does not match this actor" in newCase[CaseData]: (_, data) =>
      import data.*
      val badMeRef = MeRef(linkKey, srcFake.api, srcFake.api) // trgNode field should be trgNode, not srcFake
      trgNode.upsertEdgeTrg[IO](badMeRef, props1)
        .asserting: _ =>
          val (source, err) = fakeManager.expectReportedError
          source mustBe trgNode
          err.getMessage must include("Edge target node does not match this node")