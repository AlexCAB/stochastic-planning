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
| created: 2026-01-17 |||||||||||*/

package planning.engine.planner.map.dcg

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.test.data.{MapDcgNodeTestData, MapSampleTestData}
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.{Indexies, Links, Thens}
import planning.engine.planner.map.dcg.nodes.DcgNode

class DcgGraphSpec extends UnitSpecWithData:
  private class CaseData extends Case with MapDcgNodeTestData with MapSampleTestData:
    lazy val emptyDcgGraph: DcgGraph[IO] = DcgGraph.empty[IO]

    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)
    lazy val hnId4 = HnId(4)
    lazy val hnId5 = HnId(5)
    lazy val nuHnId = HnId(-1)

    lazy val endsSet = Set(EndIds(hnId1, hnId2), EndIds(hnId1, hnId3), EndIds(hnId2, hnId2))
    lazy val absNodes: List[DcgNode.Abstract[IO]] = List(hnId1, hnId2, hnId3).map(id => makeAbstractDcgNode(id = id))
    lazy val conNodes: List[DcgNode.Concrete[IO]] = List(hnId4, hnId5).map(id => makeConcreteDcgNode(id = id))

    lazy val graphWithNodes: DcgGraph[IO] = emptyDcgGraph
      .addAbsNodes(absNodes)
      .flatMap(_.addConNodes(conNodes))
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
      ends = EndIds(snId, tnId),
      links = Links(sIds.map(sId => makeSampleRecord(sId, snId, tnId)).toMap),
      thens = Thens.empty
    )

    lazy val dcgEdges: List[DcgEdgeData] = List(
      makeDcgEdge(hnId1, hnId2, List(sampleId1, sampleId2)),
      makeDcgEdge(hnId2, hnId3, List(sampleId1)),
      makeDcgEdge(hnId2, hnId4, List(sampleId2)),
      makeDcgEdge(hnId2, hnId5, List(sampleId2))
    )

    lazy val dcgLinkEdge: DcgEdgeData = dcgEdges.head
    lazy val graphWithEdges: DcgGraph[IO] = graphWithNodes.addEdges(dcgEdges).unsafeRunSync()

    lazy val List(n1, n2, n3) = List((1, 101), (2, 101), (3, 102)).map:
      case (id, ioIdx) => makeConcreteDcgNode(id = HnId(id), valueIndex = IoIndex(ioIdx))

    lazy val List(s1, s2, s3) = List(1, 2, 3).map(id => makeSampleData(id = SampleId(id)))
    lazy val nodes = List(1, 2, 3).map(id => makeAbstractDcgNode(id = HnId(id)))

  "DcgGraph.allHnIds" should:
    "return all HnIds" in newCase[CaseData]: (tn, data) =>
      val testConcreteNode = data.makeConcreteDcgNode()
      val testAbstractNode = data.makeAbstractDcgNode()

      val graph = data.emptyDcgGraph.copy(
        concreteNodes = Map(testConcreteNode.id -> testConcreteNode),
        abstractNodes = Map(testAbstractNode.id -> testAbstractNode)
      )

      async[IO]:
        logInfo(tn, s"graph: $graph").await
        graph.allHnIds mustBe Set(testConcreteNode.id, testAbstractNode.id)

  "DcgGraph.allSampleIds" should:
    "return all SampleIds" in newCase[CaseData]: (tn, data) =>
      val testSample1 = data.makeSampleData(SampleId(5001))
      val testSample2 = data.makeSampleData(SampleId(5002))

      val graph = data.emptyDcgGraph.copy(
        samplesData = Map(testSample1.id -> testSample1, testSample2.id -> testSample2)
      )

      async[IO]:
        logInfo(tn, s"graph: $graph").await
        graph.allSampleIds mustBe Set(testSample1.id, testSample2.id)

  "DcgGraph.isEmpty" should:
    "return true for empty graph" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.pure[IO].asserting(_.isEmpty mustBe true)

    "return false for non empty graph" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.pure[IO].asserting(_.isEmpty mustBe false)

  "DcgGraph.checkEdges(...)" should:
    "check edges and return no error for valid" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.checkEdges(data.dcgEdges).logValue(tn).assertNoException

    "fail if duplicate edge keys" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.checkEdges(data.dcgEdges :+ data.dcgEdges.head).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

    "fail if edge connects to non existing hidden nodes" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.nuHnId, data.nuHnId, List(data.sampleId1))
      data.graphWithNodes.checkEdges(data.dcgEdges :+ invalidEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge refers to unknown HnIds"))

    "fail if duplicate sample IDs for edge between same HnIds" in newCase[CaseData]: (tn, data) =>
      val duplicateSampleEdge = data.makeDcgEdge(data.hnId1, data.hnId2, List(data.sampleId1))
      data.graphWithNodes.checkEdges(data.dcgEdges :+ duplicateSampleEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

  "DcgGraph.addConcreteNodes(...)" should:
    "add concrete nodes" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = data.emptyDcgGraph.addConNodes(List(data.n1, data.n2, data.n3)).await
        logInfo(tn, s"graph: $graph").await

        graph.concreteNodes mustBe List(data.n1, data.n2, data.n3).map(n => n.id -> n).toMap

    "fail if node IDs is not distinct" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addConNodes(List(data.n1, data.n1)).logValue(tn).assertThrows[AssertionError]

    "fail if concrete nodes that already exist" in newCase[CaseData]: (tn, data) =>
      data
        .emptyDcgGraph.copy(concreteNodes = Map(data.n1.id -> data.n1))
        .addConNodes(List(data.n1))
        .logValue(tn)
        .assertThrows[AssertionError]

    "DcgState.addAbstractNodes(...)" should:
      "add abstract nodes" in newCase[CaseData]: (tn, data) =>
        async[IO]:
          val graph = data.emptyDcgGraph.addAbsNodes(data.nodes).await
          logInfo(tn, s"graph: $graph").await

          graph.abstractNodes mustBe data.nodes.map(n => n.id -> n).toMap

      "fail if node IDs is not distinct" in newCase[CaseData]: (tn, data) =>
        data.emptyDcgGraph.addAbsNodes(List(data.nodes.head, data.nodes.head)).logValue(tn)
          .assertThrows[AssertionError]

      "fail if abstract nodes that already exist" in newCase[CaseData]: (tn, data) =>
        data
          .emptyDcgGraph.copy(abstractNodes = Map(data.n1.id -> data.nodes.head))
          .addAbsNodes(List(data.nodes.head))
          .logValue(tn)
          .assertThrows[AssertionError]

  "DcgGraph.addEdges(...)" should:
    "add edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = data.graphWithNodes.addEdges(data.dcgEdges).await
        logInfo(tn, s"graph: $graph").await

        graph.edgesData mustBe data.dcgEdges.map(e => e.ends -> e).toMap

        graph.edgesMapping.forward mustBe Map(
          data.hnId2 -> Set(data.hnId3, data.hnId4, data.hnId5),
          data.hnId1 -> Set(data.hnId2)
        )

        graph.edgesMapping.backward mustBe Map(
          data.hnId4 -> Set(data.hnId2),
          data.hnId3 -> Set(data.hnId2),
          data.hnId2 -> Set(data.hnId1),
          data.hnId5 -> Set(data.hnId2)
        )

    "fail if edge keys is not distinct" in newCase[CaseData]: (tn, data) =>
      val e1 = data.dcgEdges.head
      data.graphWithNodes.addEdges(List(e1, e1)).logValue(tn).assertThrows[AssertionError]

    "fail if edges connect to non existing nodes" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addEdges(List(data.dcgEdges.head)).logValue(tn).assertThrows[AssertionError]

    "fail if edge already exist" in newCase[CaseData]: (tn, data) =>
      val e1 = data.dcgEdges.head
      data.graphWithNodes.addEdges(List(e1)).flatMap(_.addEdges(List(e1))).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.mergeEdges(...)" should:
    lazy val sampleId3 = SampleId(1003)
    lazy val sampleId4 = SampleId(1004)

    "merge edges" in newCase[CaseData]: (tn, data) =>
      lazy val dcgEdges2 = List(
        data.makeDcgEdge(data.hnId1, data.hnId2, List(sampleId3, sampleId4)),
        data.makeDcgEdge(data.hnId2, data.hnId3, List(sampleId3)),
        data.makeDcgEdge(data.hnId1, data.hnId5, List(sampleId4))
      )

      async[IO]:
        val joinedEdges: Map[EndIds, DcgEdgeData] = (data.dcgEdges ++ dcgEdges2)
          .groupBy(_.ends).view.mapValues(_.reduceLeft((e1, e2) => e1.join[IO](e2).unsafeRunSync()))
          .toMap

        val graph = data.graphWithEdges.mergeEdges(dcgEdges2).await
        logInfo(tn, s"graph: $graph").await

        graph.edgesData mustBe joinedEdges

        graph.edgesMapping.forward mustBe Map(
          data.hnId2 -> Set(data.hnId3, data.hnId4, data.hnId5),
          data.hnId1 -> Set(data.hnId2, data.hnId5)
        )

        graph.edgesMapping.backward mustBe Map(
          data.hnId2 -> Set(data.hnId1),
          data.hnId3 -> Set(data.hnId2),
          data.hnId4 -> Set(data.hnId2),
          data.hnId5 -> Set(data.hnId1, data.hnId2)
        )

  "DcgGraph.addSamples(...)" should:
    "add samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = data.emptyDcgGraph.addSamples(List(data.s1, data.s2, data.s3)).await
        logInfo(tn, s"graph: $graph").await

        graph.samplesData mustBe List(data.s1, data.s2, data.s3).map(s => s.id -> s).toMap

    "fail if sample IDs is not distinct" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addSamples(List(data.s1, data.s1)).logValue(tn).assertThrows[AssertionError]

    "fail if samples that already exist" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addSamples(List(data.s1)).flatMap(_.addSamples(List(data.s1)))
        .logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getConForHnId(...)" should:
    "get concrete node for HnId" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = data.graphWithNodes.getConForHnId(data.conNodes.head.id).await
        logInfo(tn, s"conNode: $conNode").await

        conNode mustBe data.conNodes.head

    "fail if concrete node for HnId not found" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.getConForHnId(HnId(-1)).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getAbsForHnId(...)" should:
    "get abstract node for HnId" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = data.graphWithNodes.getAbsForHnId(data.absNodes.head.id).await
        logInfo(tn, s"absNode: $absNode").await

        absNode mustBe data.absNodes.head

    "fail if abstract node for HnId not found:" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.getAbsForHnId(HnId(-1)).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.findHnIdsByNames(...)" should:
    "find HnIds by names" in newCase[CaseData]: (tn, data) =>
      val name1 = data.absNodes.head.name.get
      val name2 = HnName("unknown_name")

      async[IO]:
        val result = data.graphWithNodes.findHnIdsByNames(Set(name1, name2)).await
        logInfo(tn, s"result: $result").await

        result mustBe Map(name1 -> data.absNodes.filter(_.name.contains(name1)).map(_.id).toSet)
