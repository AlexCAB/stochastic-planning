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
| created: 23.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.state

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.state.Struct
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.test.data.{MapEdgeTestData, MapNodeTestData}

class NodeStructStateSpec extends UnitSpecWithData with MapNodeTestData with MapEdgeTestData:
  private class CaseData extends Case with WithMapNode with WithMapEdge:
    val srcMnId1: MnId.Con = MnId.Con(1L)
    val trgMnId1: MnId.Abs = MnId.Abs(2L)
    val trgMnId2: MnId.Abs = MnId.Abs(4L)

    val srcNode1: Node = makeNodeStub(srcMnId1)
    val trgNode1: Node = makeNodeStub(trgMnId1)
    val trgNode2: Node = makeNodeStub(trgMnId2)
    val trgNodeConflict: Node = makeNodeStub(trgMnId1, "Conflicting target")

    val linkKey1: MeKey = MeKey.Link(srcMnId1, trgMnId1)
    val linkKey2: MeKey = MeKey.Link(srcMnId1, trgMnId2)

    val meRef1: MeRef = MeRef(linkKey1, srcNode1, trgNode1)
    val meRef2: MeRef = MeRef(linkKey2, srcNode1, trgNode2)
    val meRefConflict: MeRef = MeRef(linkKey1, srcNode1, trgNodeConflict)

    val propsConflict: Map[SampleId, Sample.Props] = Map(props1.keySet.head -> Sample.Props(999L, 9.9))

  "State.init" should:
    "have empty maps, zero total sample count and nextHnIndex starting at 1" in newCase[CaseData]: (tn, _) =>
      IO.pure(Struct.init).logValue(tn).asserting: state =>
        state.nextHnIndex mustBe 1L
        state.incomingMap mustBe Map.empty
        state.outgoingMap mustBe Map.empty
        state.sampleMap mustBe Map.empty
        state.totalSamplesCount mustBe 0L

  "State.upsertEdgeSrc(...)" should:
    "add edge to outgoing map and sample map when empty" in newCase[CaseData]: (tn, data) =>
      import data.*
      Struct.init.upsertEdgeSrc[IO](meRef1, props1).logValue(tn).asserting: state =>
        state.incomingMap mustBe Map.empty
        state.outgoingMap mustBe Map(trgMnId1 -> Struct.EdgeData(trgNode1, props1.keySet))
        state.sampleMap.keySet mustBe props1.keySet
        state.sampleMap.values.map(_.props).toSet mustBe props1.values.toSet
        state.sampleMap.values.map(_.index).toSet mustBe Set(1, 2, 3).map(HnIndex(_))
        state.nextHnIndex mustBe 4L

    "join sample IDs when edge to same target already exists" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state = Struct
          .init.upsertEdgeSrc[IO](meRef1, props1)
          .flatMap(_.upsertEdgeSrc[IO](meRef1, props2))
          .await

        val allSampleIds = props1.keySet ++ props2.keySet

        state.outgoingMap mustBe Map(trgMnId1 -> Struct.EdgeData(trgNode1, allSampleIds))
        state.sampleMap.keySet mustBe allSampleIds
        state.sampleMap.values.map(_.props).toSet mustBe (props1.values.toSet ++ props2.values.toSet)
        state.nextHnIndex mustBe 6L

    "add edges to multiple distinct target nodes" in newCase[CaseData]: (_, data) =>
      import Struct.*
      import data.*
      async[IO]:
        val state = Struct
          .init.upsertEdgeSrc[IO](meRef1, props1)
          .flatMap(_.upsertEdgeSrc[IO](meRef2, props2))
          .await

        state.outgoingMap mustBe Map(
          trgMnId1 -> EdgeData(trgNode1, props1.keySet),
          trgMnId2 -> EdgeData(trgNode2, props2.keySet),
        )
        state.sampleMap.keySet mustBe props1.keySet ++ props2.keySet
        state.nextHnIndex mustBe 6L

    "leave state unchanged when the same edge and samples are upserted again" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state1 = Struct.init.upsertEdgeSrc[IO](meRef1, props1).await
        val state2 = Struct.init.upsertEdgeSrc[IO](meRef1, props1).await

        state1 mustBe state2

    "fail when the same target id is upserted with a conflicting node reference" in newCase[CaseData]: (tn, data) =>
      import data.*
      Struct.init.upsertEdgeSrc[IO](meRef1, props1)
        .flatMap(_.upsertEdgeSrc[IO](meRefConflict, props2))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge reference mismatch"))

    "fail when a sample id already exists with different properties" in newCase[CaseData]: (tn, data) =>
      import data.*
      Struct.init.upsertEdgeSrc[IO](meRef1, props1)
        .flatMap(_.upsertEdgeSrc[IO](meRef1, propsConflict))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("already exists with different properties"))

  "State.upsertEdgeTrg(...)" should:
    "add edge to incoming map and sample map when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      Struct.init.upsertEdgeTrg[IO](meRef1, props1).asserting: state =>
        state.incomingMap mustBe Map(srcMnId1 -> Struct.EdgeData(srcNode1, props1.keySet))
        state.outgoingMap mustBe Map.empty
        state.sampleMap.keySet mustBe props1.keySet
        state.sampleMap.values.map(_.props).toSet mustBe props1.values.toSet
        state.sampleMap.values.map(_.index).toSet mustBe Set(1, 2, 3).map(HnIndex(_))
        state.nextHnIndex mustBe 4L

  "State.withTotalSamplesCount(...)" should:
    "update only the totalSamplesCount field" in newCase[CaseData]: (_, _) =>
      Struct.init.withTotalSamplesCount[IO](42L)
        .asserting(_ mustBe Struct.init.copy(totalSamplesCount = 42L))
