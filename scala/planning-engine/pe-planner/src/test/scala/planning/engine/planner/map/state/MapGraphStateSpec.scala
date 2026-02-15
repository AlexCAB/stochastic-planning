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
| created: 2025-12-13 |||||||||||*/

package planning.engine.planner.map.state

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.test.data.{MapDcgTestData, MapSampleTestData}
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.common.values.edge.EdgeKey
import planning.engine.planner.map.dcg.edges.DcgSamples.{Indexies, Links, Thens}
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.state.MapGraphState

class MapGraphStateSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapDcgTestData with MapSampleTestData:
    lazy val emptyDcgState: MapGraphState[IO] = MapGraphState.empty[IO]

    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)
    lazy val hnId4 = HnId(4)
    lazy val hnId5 = HnId(5)
    lazy val nuHnId = HnId(-1)

    lazy val absNodes: List[DcgNode.Abstract[IO]] = List(hnId1, hnId2, hnId3).map(id => makeAbstractDcgNode(id = id))
    lazy val conNodes: List[DcgNode.Concrete[IO]] = List(hnId4, hnId5).map(id => makeConcreteDcgNode(id = id))

    lazy val stateWithNodes: MapGraphState[IO] = emptyDcgState
      .addAbstractNodes(absNodes)
      .flatMap(_.addConcreteNodes(conNodes))
      .unsafeRunSync()

    lazy val sampleId1 = SampleId(1001)
    lazy val sampleId2 = SampleId(1002)

    def makeIndexiesMap(sId: SampleId): Map[HnId, HnIndex] = List(hnId1, hnId2, hnId3, hnId4, hnId5, nuHnId)
      .zipWithIndex.map((id, i) => id -> HnIndex(100 + sId.value + i + 1)).toMap

    def makeSampleRecord(sId: SampleId, snId: HnId, tnId: HnId): (SampleId, Indexies) =
      val ixMap = makeIndexiesMap(sId)
      sId -> Indexies(
        src = ixMap.getOrElse(snId, fail(s"Source HnId index not found, for $snId")),
        trg = ixMap.getOrElse(tnId, fail(s"Target HnId index not found, for $tnId"))
      )

    def makeDcgEdge(snId: HnId, tnId: HnId, sIds: List[SampleId]): DcgEdgeData = DcgEdgeData(
      ends = EdgeKey(snId, tnId),
      links = Links(sIds.map(sId => makeSampleRecord(sId, snId, tnId)).toMap),
      thens = Thens.empty
    )

    lazy val dcgEdges: List[DcgEdgeData] = List(
      makeDcgEdge(hnId1, hnId2, List(sampleId1, sampleId2)),
      makeDcgEdge(hnId2, hnId3, List(sampleId1)),
      makeDcgEdge(hnId2, hnId4, List(sampleId2)),
      makeDcgEdge(hnId2, hnId5, List(sampleId2))
    )

    lazy val stateWithEdges: MapGraphState[IO] = stateWithNodes.addEdges(dcgEdges).unsafeRunSync()

    lazy val List(n1, n2, n3) = List((1, 101), (2, 101), (3, 102)).map:
      case (id, ioIdx) => makeConcreteDcgNode(id = HnId(id), valueIndex = IoIndex(ioIdx))

    lazy val List(s1, s2, s3) = List(1, 2, 3).map(id => makeSampleData(id = SampleId(id)))
    lazy val nodes = List(1, 2, 3).map(id => makeAbstractDcgNode(id = HnId(id)))

  "MapGraphState.isEmpty" should:
    "return true for empty state" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.pure[IO].asserting(_.isEmpty mustBe true)

    "return false for non empty state" in newCase[CaseData]: (tn, data) =>
      data.stateWithNodes.pure[IO].asserting(_.isEmpty mustBe false)

  "MapGraphState.addConcreteNodes(...)" should:
    "add concrete nodes" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val state = data.emptyDcgState.addConcreteNodes(List(data.n1, data.n2, data.n3)).await
        logInfo(tn, s"state: $state").await

        state.ioValues mustBe Map(data.n1.ioValue -> Set(data.n1.id, data.n2.id), data.n3.ioValue -> Set(data.n3.id))
        state.graph.concreteNodes mustBe List(data.n1, data.n2, data.n3).map(n => n.id -> n).toMap

    "fail if ioValues already exist" in newCase[CaseData]: (tn, data) =>
      data
        .emptyDcgState.addConcreteNodes(List(data.n1))
        .flatMap(stateWithN1 => stateWithN1.addConcreteNodes(List(data.n2)))
        .logValue(tn)
        .assertThrows[AssertionError]

    "DcgState.addAbstractNodes(...)" should:
      "add abstract nodes" in newCase[CaseData]: (tn, data) =>
        async[IO]:
          val state = data.emptyDcgState.addAbstractNodes(data.nodes).await
          logInfo(tn, s"state: $state").await

          state.graph.abstractNodes mustBe data.nodes.map(n => n.id -> n).toMap

  "MapGraphState.addEdges(...)" should:
    "add edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val state = data.stateWithNodes.addEdges(data.dcgEdges).await
        logInfo(tn, s"state: $state").await

        state.graph.edgesData mustBe data.dcgEdges.map(e => e.ends -> e).toMap

        state.graph.edgesMapping.forward mustBe Map(
          data.hnId2 -> Set(data.hnId3, data.hnId4, data.hnId5),
          data.hnId1 -> Set(data.hnId2)
        )

        state.graph.edgesMapping.backward mustBe Map(
          data.hnId4 -> Set(data.hnId2),
          data.hnId3 -> Set(data.hnId2),
          data.hnId2 -> Set(data.hnId1),
          data.hnId5 -> Set(data.hnId2)
        )

  "MapGraphState.mergeEdges(...)" should:
    lazy val sampleId3 = SampleId(1003)
    lazy val sampleId4 = SampleId(1004)

    "merge edges" in newCase[CaseData]: (tn, data) =>
      lazy val dcgEdges2 = List(
        data.makeDcgEdge(data.hnId1, data.hnId2, List(sampleId3, sampleId4)),
        data.makeDcgEdge(data.hnId2, data.hnId3, List(sampleId3)),
        data.makeDcgEdge(data.hnId1, data.hnId5, List(sampleId4))
      )

      async[IO]:
        val joinedEdges: Map[EdgeKey, DcgEdgeData] = (data.dcgEdges ++ dcgEdges2)
          .groupBy(_.ends).view.mapValues(_.reduceLeft((e1, e2) => e1.join[IO](e2).unsafeRunSync()))
          .toMap

        val state = data.stateWithEdges.mergeEdges(dcgEdges2).await
        logInfo(tn, s"state: $state").await

        state.graph.edgesData mustBe joinedEdges

        state.graph.edgesMapping.forward mustBe Map(
          data.hnId2 -> Set(data.hnId3, data.hnId4, data.hnId5),
          data.hnId1 -> Set(data.hnId2, data.hnId5)
        )

        state.graph.edgesMapping.backward mustBe Map(
          data.hnId2 -> Set(data.hnId1),
          data.hnId3 -> Set(data.hnId2),
          data.hnId4 -> Set(data.hnId2),
          data.hnId5 -> Set(data.hnId1, data.hnId2)
        )

  "MapGraphState.addSamples(...)" should:
    "add samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val state = data.emptyDcgState.addSamplesData(List(data.s1, data.s2, data.s3)).await
        logInfo(tn, s"state: $state").await

        state.graph.samplesData mustBe List(data.s1, data.s2, data.s3).map(s => s.id -> s).toMap

    "fail if samples that already exist" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.addSamplesData(List(data.s1)).flatMap(_.addSamplesData(List(data.s1)))
        .logValue(tn).assertThrows[AssertionError]

  "DcgState.findConForIoValues(...)" should:
    "get concrete nodes for IoValues" in newCase[CaseData]: (tn, data) =>
      val ioValue1 = data.conNodes.head.ioValue
      val ioValue2 = IoValue(IoName("not_in_set"), IoIndex(123))

      async[IO]:
        val (map, notFound) = data.stateWithNodes.findConForIoValues(Set(ioValue1, ioValue2)).await
        logInfo(tn, s"map: $map, notFound: $notFound").await

        map mustBe Map(ioValue1 -> data.conNodes.filter(_.ioValue == ioValue1).toSet)
        notFound mustBe Set(ioValue2)
