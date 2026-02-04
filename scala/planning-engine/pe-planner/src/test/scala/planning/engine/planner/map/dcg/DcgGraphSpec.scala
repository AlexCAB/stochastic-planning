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
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.test.data.{MapDcgTestData, MapSampleTestData}
import planning.engine.planner.map.dcg.edges.{DcgEdgeData, DcgEdgesMapping}
import planning.engine.common.values.edges.Edge
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.{Indexies, Links, Thens}
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.dcg.samples.DcgSample

class DcgGraphSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case with MapDcgTestData with MapSampleTestData:
    lazy val emptyDcgGraph: DcgGraph[IO] = DcgGraph.empty[IO]

    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)
    lazy val hnId4 = HnId(4)
    lazy val hnId5 = HnId(5)
    lazy val nuHnId = HnId(-1)

    lazy val endsSet = Set(Edge.Ends(hnId1, hnId2), Edge.Ends(hnId1, hnId3), Edge.Ends(hnId2, hnId2))
    lazy val conNodes: List[DcgNode.Concrete[IO]] = List(hnId1, hnId2).map(id => makeConcreteDcgNode(id = id))
    lazy val absNodes: List[DcgNode.Abstract[IO]] = List(hnId3, hnId4, hnId5).map(id => makeAbstractDcgNode(id = id))

    lazy val graphWithNodes: DcgGraph[IO] = emptyDcgGraph
      .addAbsNodes(absNodes)
      .flatMap(_.addConNodes(conNodes))
      .unsafeRunSync()

    lazy val sampleId1 = SampleId(1001)
    lazy val sampleId2 = SampleId(1002)
    lazy val sampleId3 = SampleId(1003)

    def makeIndexiesMap(sId: SampleId): Map[HnId, HnIndex] = List(hnId1, hnId2, hnId3, hnId4, hnId5, nuHnId)
      .zipWithIndex.map((id, i) => id -> HnIndex(100 + sId.value + i + 1)).toMap

    def makeSampleRecord(sId: SampleId, snId: HnId, tnId: HnId): (SampleId, Indexies) =
      val ixMap = makeIndexiesMap(sId)
      sId -> Indexies(
        src = ixMap.getOrElse(snId, fail(s"Source HnId index not found, for $snId")),
        trg = ixMap.getOrElse(tnId, fail(s"Target HnId index not found, for $tnId"))
      )

    def makeDcgEdge(snId: HnId, tnId: HnId, lIds: List[SampleId], tIds: List[SampleId]): DcgEdgeData = DcgEdgeData(
      ends = Edge.Ends(snId, tnId),
      links = Links(lIds.map(sId => makeSampleRecord(sId, snId, tnId)).toMap),
      thens = Thens(tIds.map(sId => makeSampleRecord(sId, snId, tnId)).toMap)
    )

    lazy val dcgLinkEdge1: DcgEdgeData = makeDcgEdge(hnId1, hnId3, List(sampleId1, sampleId2), List())
    lazy val dcgLinkEdge2: DcgEdgeData = makeDcgEdge(hnId2, hnId4, List(sampleId1), List())
    lazy val dcgLinkEdge3: DcgEdgeData = makeDcgEdge(hnId4, hnId5, List(sampleId2), List())

    lazy val dcgEdges: List[DcgEdgeData] = List(
      // Link edges
      dcgLinkEdge1,
      dcgLinkEdge2,
      dcgLinkEdge3,
      makeDcgEdge(hnId2, hnId5, List(sampleId2), List()),
      // Then edges
      makeDcgEdge(hnId2, hnId1, List(), List(sampleId2)),
      makeDcgEdge(hnId5, hnId1, List(), List(sampleId1, sampleId2))
    )

    lazy val sampleData: List[SampleData] = List(sampleId1, sampleId2).map(id => makeSampleData(id = id))

    lazy val graphWithEdges: DcgGraph[IO] = graphWithNodes
      .addEdges(dcgEdges)
      .flatMap(_.addSamplesData(sampleData))
      .unsafeRunSync()

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

  "DcgGraph.allIndexies" should:
    "return all HnIndexies" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.allIndexies.pure[IO].logValue(tn).asserting: allIndexies =>
        allIndexies must not be empty
        allIndexies.keySet mustBe data.graphWithEdges.allHnIds

        allIndexies mustBe data.graphWithEdges.edgesData.values.toList
          .flatMap(e => List(e.ends.src -> e.srcHnIndex, e.ends.trg -> e.trgHnIndex))
          .groupBy(_._1)
          .map((hdId, lst) => hdId -> lst.flatMap(_._2).toSet)

  "DcgGraph.isEmpty" should:
    "return true for empty graph" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.pure[IO].asserting(_.isEmpty mustBe true)

    "return false for non empty graph" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.pure[IO].asserting(_.isEmpty mustBe false)

  "DcgGraph.validationName" should:
    "return correct name" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.checkValidationName("DcgGraph", tn)

  "DcgGraph.validationErrors" should:
    "return no errors for valid graph" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.checkNoValidationError(tn)

    "return error when concrete nodes map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      data
        .graphWithEdges.copy(concreteNodes = data.graphWithEdges.concreteNodes + (data.nuHnId -> data.conNodes.head))
        .checkOneValidationError("Concrete nodes map keys and values IDs mismatch", tn)

    "return error when abstract nodes map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      data
        .graphWithEdges.copy(abstractNodes = data.graphWithEdges.abstractNodes + (data.nuHnId -> data.absNodes.head))
        .checkOneValidationError("Abstract nodes map keys and values IDs mismatch", tn)

    "return error when concrete and abstract nodes IDs overlap detected" in newCase[CaseData]: (tn, data) =>
      val overlappingHnId = data.hnId1
      val overlappingConNode = data.makeConcreteDcgNode(id = overlappingHnId)
      val overlappingAbsNode = data.makeAbstractDcgNode(id = overlappingHnId)

      data.emptyDcgGraph.copy(
        concreteNodes = Map(overlappingHnId -> overlappingConNode),
        abstractNodes = Map(overlappingHnId -> overlappingAbsNode)
      ).checkOneValidationError("Concrete and Abstract nodes IDs overlap detected", tn)

    "return error when edges data map keys and values ends mismatch" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.copy(
        edgesData = data.graphWithEdges.edgesData + (Edge.Ends(data.hnId3, data.hnId3) -> data.dcgLinkEdge1)
      ).checkOneOfValidationErrors("Edges data map keys and values ends mismatch", tn)

    "return error when edge refers to unknown HnIds" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.nuHnId, data.nuHnId, List(data.sampleId1), List())

      data.graphWithEdges.copy(
        edgesData = data.graphWithEdges.edgesData + (invalidEdge.ends -> invalidEdge)
      ).checkOneOfValidationErrors("Edge refers to unknown HnIds", tn)

    "return error when edges mapping refers to unknown HnIds and unknown edge ends" in newCase[CaseData]: (tn, data) =>
      val invalidGraph = data.graphWithEdges.copy(
        edgesMapping = data.graphWithEdges.edgesMapping.copy(
          forward = data.graphWithEdges.edgesMapping.forward + (data.nuHnId -> Set(data.hnId1)),
          backward = data.graphWithEdges.edgesMapping.backward + (data.hnId1 -> Set(data.nuHnId))
        )
      )
      async[IO]:
        invalidGraph.checkOneOfValidationErrors("Edges mapping refers to unknown HnIds", tn).await
        invalidGraph.checkOneOfValidationErrors("Edges mapping refers to unknown edge ends", tn).await

    "return error when samples data map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.copy(
        samplesData = data.graphWithEdges.samplesData + (data.sampleId1 -> data.makeSampleData(SampleId(9999)))
      ).checkOneValidationError("Samples data map keys and values IDs mismatch", tn)

    "return error when some sample IDs used in edges are not found" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.hnId1, data.hnId3, List(data.sampleId1, SampleId(9999)), List())

      data.graphWithEdges.copy(
        edgesData = data.graphWithEdges.edgesData + (invalidEdge.ends -> invalidEdge)
      ).checkOneValidationError("Some sample IDs used in edges are not found", tn)

  "DcgGraph.checkEdges(...)" should:
    "check edges and return no error for valid" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.checkEdges(data.dcgEdges).logValue(tn).assertNoException

    "fail if duplicate edge keys" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.checkEdges(data.dcgEdges :+ data.dcgEdges.head).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

    "fail if edge connects to non existing hidden nodes" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.nuHnId, data.nuHnId, List(data.sampleId1), List())
      data.graphWithNodes.checkEdges(data.dcgEdges :+ invalidEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge refers to unknown HnIds"))

    "fail if duplicate sample IDs for edge between same HnIds" in newCase[CaseData]: (tn, data) =>
      val duplicateSampleEdge = data.makeDcgEdge(data.hnId1, data.hnId3, List(data.sampleId1), List())
      data.graphWithNodes.checkEdges(data.dcgEdges :+ duplicateSampleEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

  "DcgGraph.joinEdges" should:
    val sampleId3 = SampleId(1003)
    val sampleId4 = SampleId(1004)

    "join edges correctly" in newCase[CaseData]: (tn, data) =>

      val ends1 = data.dcgEdges.head.ends
      val ends2 = data.dcgEdges(1).ends

      val newEdges = List
        .apply((ends1, sampleId3), (ends2, sampleId4))
        .map((ends, sampleIds) => data.makeDcgEdge(ends.src, ends.trg, List(sampleIds), List()))

      async[IO]:
        val joinedEdges: Map[Edge.Ends, DcgEdgeData] = data.emptyDcgGraph
          .joinEdges(data.graphWithEdges.edgesData, newEdges)
          .await

        logInfo(tn, s"joinedEdges: $joinedEdges").await

        val joinedEdge1 = joinedEdges.getOrElse(ends1, fail(s"Joined edge not found, for $ends1"))
        val joinedEdge2 = joinedEdges.getOrElse(ends2, fail(s"Joined edge not found, for $ends2"))

        joinedEdge1.links.indexies.keySet mustBe Set(data.sampleId1, data.sampleId2, sampleId3)
        joinedEdge2.links.indexies.keySet mustBe Set(data.sampleId1, sampleId4)

    "fail if edge to merge not found" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.hnId5, data.hnId5, List(sampleId3), List())
      data
        .emptyDcgGraph
        .joinEdges(data.graphWithEdges.edgesData, List(invalidEdge))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge to merge not found"))

  "DcgGraph.getEdges(...)" should:
    "get edges correctly" in newCase[CaseData]: (tn, data) =>
      val ends = Set(Edge.Ends(data.hnId1, data.hnId3), Edge.Ends(data.hnId2, data.hnId4))

      async[IO]:
        val edges: Map[Edge.Ends, DcgEdgeData] = data.graphWithEdges.getEdges(ends).await
        logInfo(tn, s"edges: $edges").await

        edges.keySet mustBe ends
        edges(Edge.Ends(data.hnId1, data.hnId3)) mustBe data.dcgEdges.head
        edges(Edge.Ends(data.hnId2, data.hnId4)) mustBe data.dcgEdges(1)

  "DcgGraph.nextHnIndexies(...)" should:
    "return next HnIndex map correctly for all nodes" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val nextIndexies: Map[HnId, HnIndex] = data.graphWithEdges.nextHnIndexies(data.graphWithEdges.allHnIds).await
        logInfo(tn, s"nextIndexies: $nextIndexies").await

        nextIndexies mustBe Map(
          data.hnId1 -> HnIndex(1104),
          data.hnId2 -> HnIndex(1105),
          data.hnId3 -> HnIndex(1106),
          data.hnId4 -> HnIndex(1107),
          data.hnId5 -> HnIndex(1108)
        )

    "return next HnIndex map correctly for subset of nodes" in newCase[CaseData]: (tn, data) =>
      val hnIds = Set(data.hnId2, data.hnId5)

      async[IO]:
        val nextIndexies: Map[HnId, HnIndex] = data.graphWithEdges.nextHnIndexies(hnIds).await
        logInfo(tn, s"nextIndexies: $nextIndexies").await

        nextIndexies mustBe Map(
          data.hnId2 -> HnIndex(1105),
          data.hnId5 -> HnIndex(1108)
        )

    "fail if HN not found" in newCase[CaseData]: (tn, data) =>
      data
        .graphWithEdges
        .nextHnIndexies(Set(data.nuHnId))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some HnIds are not found in the graph"))

  "DcgGraph.addConNodes(...)" should:
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

    "DcgState.addAbsNodes(...)" should:
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
          data.hnId1 -> Set(data.hnId3),
          data.hnId2 -> Set(data.hnId1, data.hnId4, data.hnId5),
          data.hnId4 -> Set(data.hnId5),
          data.hnId5 -> Set(data.hnId1)
        )

        graph.edgesMapping.backward mustBe Map(
          data.hnId1 -> Set(data.hnId2, data.hnId5),
          data.hnId3 -> Set(data.hnId1),
          data.hnId4 -> Set(data.hnId2),
          data.hnId5 -> Set(data.hnId2, data.hnId4)
        )

    "fail if edge keys is not distinct" in newCase[CaseData]: (tn, data) =>
      val e1 = data.dcgEdges.head
      data.graphWithNodes.addEdges(List(e1, e1)).logValue(tn).assertThrows[AssertionError]

    "fail if edges connect to non existing nodes" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addEdges(List(data.dcgEdges.head)).logValue(tn).assertThrows[AssertionError]

    "fail if edge already exist" in newCase[CaseData]: (tn, data) =>
      val e1 = data.dcgEdges.head
      data.graphWithNodes.addEdges(List(e1)).flatMap(_.addEdges(List(e1))).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.updateEdges(...)" should:
    "update edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val updatedEdge = data.makeDcgEdge(data.hnId1, data.hnId3, List(data.sampleId2), List(data.sampleId3))
        val graph = data.graphWithEdges.updateEdges(List(updatedEdge)).await
        logInfo(tn, s"graph: $graph").await
        graph.edgesData.getOrElse(updatedEdge.ends, fail("Updated edge not found")) mustBe updatedEdge

    "fail if edge to update not found" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.hnId5, data.hnId5, List(data.sampleId1), List())
      data.graphWithEdges.updateEdges(List(invalidEdge)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some edges to update do not exist in the graph"))

  "DcgGraph.mergeEdges(...)" should:
    lazy val sampleId3 = SampleId(1003)
    lazy val sampleId4 = SampleId(1004)

    "merge edges" in newCase[CaseData]: (tn, data) =>
      lazy val dcgEdges2 = List(
        data.makeDcgEdge(data.hnId1, data.hnId3, List(sampleId3, sampleId4), List()),
        data.makeDcgEdge(data.hnId2, data.hnId4, List(sampleId3), List()),
        data.makeDcgEdge(data.hnId1, data.hnId5, List(sampleId4), List())
      )

      async[IO]:
        val joinedEdges: Map[Edge.Ends, DcgEdgeData] = (data.dcgEdges ++ dcgEdges2)
          .groupBy(_.ends).view.mapValues(_.reduceLeft((e1, e2) => e1.join[IO](e2).unsafeRunSync()))
          .toMap

        val graph = data.graphWithEdges.mergeEdges(dcgEdges2).await
        logInfo(tn, s"graph: $graph").await

        graph.edgesData mustBe joinedEdges

        graph.edgesMapping.forward mustBe Map(
          data.hnId1 -> Set(data.hnId3, data.hnId5),
          data.hnId2 -> Set(data.hnId1, data.hnId4, data.hnId5),
          data.hnId4 -> Set(data.hnId5),
          data.hnId5 -> Set(data.hnId1)
        )

        graph.edgesMapping.backward mustBe Map(
          data.hnId1 -> Set(data.hnId2, data.hnId5),
          data.hnId3 -> Set(data.hnId1),
          data.hnId4 -> Set(data.hnId2),
          data.hnId5 -> Set(data.hnId1, data.hnId2, data.hnId4)
        )

  "DcgGraph.addSampleData(...)" should:
    "add sample" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = data.graphWithEdges.addSampleData(data.makeSampleData(id = data.sampleId3)).await
        logInfo(tn, s"graph: $graph").await

        graph.samplesData must contain key data.sampleId3

    "fail if sample already exist" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.addSampleData(data.makeSampleData(id = data.sampleId1)).logValue(tn)
        .assertThrows[AssertionError]

  "DcgGraph.addSamplesData(...)" should:
    "add samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = data.emptyDcgGraph.addSamplesData(List(data.s1, data.s2, data.s3)).await
        logInfo(tn, s"graph: $graph").await

        graph.samplesData mustBe List(data.s1, data.s2, data.s3).map(s => s.id -> s).toMap

    "fail if sample IDs is not distinct" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addSamplesData(List(data.s1, data.s1)).logValue(tn).assertThrows[AssertionError]

    "fail if samples that already exist" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.addSamplesData(List(data.s1)).flatMap(_.addSamplesData(List(data.s1)))
        .logValue(tn).assertThrows[AssertionError]

  "DcgGraph.addSample(...)" should:
    "add sample" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val sampleData = data.makeSampleData(id = data.sampleId3)
        val ends1 = data.dcgLinkEdge2.ends
        val ends2 = data.dcgLinkEdge3.ends

        val edges = Set(ends1.toThen, ends2.toLink)

        val graph: DcgGraph[IO] = data.graphWithEdges.addSample(DcgSample(sampleData, edges)).await
        logInfo(tn, s"graph.edgesData: ${graph.edgesData}").await
        logInfo(tn, s"graph.samplesData: ${graph.samplesData}").await

        graph.samplesData must contain key data.sampleId3

        // Edge 1 before adding sample
        data.graphWithEdges.edgesData(ends1).links.indexies mustBe Map(
          data.sampleId1 -> Indexies(HnIndex(1103), HnIndex(1105)),
        )

        data.graphWithEdges.edgesData(ends1).thens.indexies mustBe Map()

        // Edge 1 after adding sample
        graph.edgesData(ends1).thens.indexies mustBe Map(
          data.sampleId3 -> Indexies(HnIndex(1105), HnIndex(1107)) // new sample
        )

        // Edge 1 before adding sample
        data.graphWithEdges.edgesData(ends2).links.indexies mustBe Map(
          data.sampleId2 -> Indexies(HnIndex(1106), HnIndex(1107))
        )

        // Edge 1 after adding sample
        graph.edgesData(ends2).links.indexies mustBe Map(
          data.sampleId2 -> Indexies(HnIndex(1106), HnIndex(1107)),
          data.sampleId3 -> Indexies(HnIndex(1107), HnIndex(1108)) // new sample
        )

  "DcgGraph.getConForHnId(...)" should:
    "get concrete node for HnId" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = data.graphWithNodes.getConForHnId(data.conNodes.head.id).await
        logInfo(tn, s"conNode: $conNode").await

        conNode mustBe data.conNodes.head

    "fail if concrete node for HnId not found" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.getConForHnId(HnId(-1)).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getConForHnIds(...)" should:
    "get concrete nodes for HnIds" in newCase[CaseData]: (tn, data) =>
      val hnIds = data.conNodes.map(_.id).toSet

      async[IO]:
        val conNodes: Map[HnId, DcgNode.Concrete[IO]] = data.graphWithNodes.getConForHnIds(hnIds).await
        logInfo(tn, s"conNodes: $conNodes").await
        conNodes mustBe data.conNodes.map(n => n.id -> n).toMap

    "fail if some concrete nodes for HnIds not found" in newCase[CaseData]: (tn, data) =>
      val hnIds = data.conNodes.map(_.id).toSet + HnId(-1)
      data.graphWithNodes.getConForHnIds(hnIds).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getAbsForHnId(...)" should:
    "get abstract node for HnId" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = data.graphWithNodes.getAbsForHnId(data.absNodes.head.id).await
        logInfo(tn, s"absNode: $absNode").await

        absNode mustBe data.absNodes.head

    "fail if abstract node for HnId not found:" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.getAbsForHnId(HnId(-1)).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getAbsForHnIds(...)" should:
    "get abstract nodes for HnIds" in newCase[CaseData]: (tn, data) =>
      val hnIds = data.absNodes.map(_.id).toSet

      async[IO]:
        val absNodes: Map[HnId, DcgNode.Abstract[IO]] = data.graphWithNodes.getAbsForHnIds(hnIds).await
        logInfo(tn, s"absNodes: $absNodes").await
        absNodes mustBe data.absNodes.map(n => n.id -> n).toMap

    "fail if some abstract nodes for HnIds not found" in newCase[CaseData]: (tn, data) =>
      val hnIds = data.absNodes.map(_.id).toSet + HnId(-1)
      data.graphWithNodes.getAbsForHnIds(hnIds).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getEdge(...)" should:
    "get edge" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val edge: DcgEdgeData = data.graphWithEdges.getEdge(data.dcgEdges.head.ends).await
        logInfo(tn, s"edge: $edge").await
        edge mustBe data.dcgEdges.head

    "fail if edge not found" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.getEdge(Edge.Ends(HnId(-1), HnId(-2))).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.getSamples(...)" should:
    "find samples" in newCase[CaseData]: (tn, data) =>
      val sampleIds = Set(data.sampleId1, data.sampleId2)

      async[IO]:
        val result: Map[SampleId, SampleData] = data.graphWithEdges.getSamples(sampleIds).await
        logInfo(tn, s"result: $result").await

        result.keySet mustBe Set(data.sampleId1, data.sampleId2)

  "DcgGraph.findHnIdsByNames(...)" should:
    "find HnIds by names" in newCase[CaseData]: (tn, data) =>
      val name1 = data.absNodes.head.name.get
      val name2 = HnName("unknown_name")

      async[IO]:
        val result = data.graphWithNodes.findHnIdsByNames(Set(name1, name2)).await
        logInfo(tn, s"result: $result").await

        result mustBe Map(name1 -> data.absNodes.filter(_.name.contains(name1)).map(_.id).toSet)

  "DcgGraph.findForwardLinkEdges(...)" should:
    "find forward link edges" in newCase[CaseData]: (tn, data) =>
      val sourceHnIds = Set(data.hnId1, data.hnId2)

      async[IO]:
        val result: Map[Edge.Ends, DcgEdgeData] = data.graphWithEdges.findForwardLinkEdges(sourceHnIds).await
        logInfo(tn, s"result: $result").await

        val expectedEnds = data.dcgEdges.filter(e => sourceHnIds.contains(e.ends.src) && e.isLink).map(_.ends).toSet

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds

  "DcgGraph.findForwardActiveLinkEdges(...)" should:
    "find forward active link edges" in newCase[CaseData]: (tn, data) =>
      val sourceHnIds = Set(data.hnId1, data.hnId2)
      val activeSampleIds = Set(data.sampleId1)

      async[IO]:
        val result: Map[Edge.Ends, DcgEdgeData] = data.graphWithEdges
          .findForwardActiveLinkEdges(sourceHnIds, activeSampleIds).await
        logInfo(tn, s"result: $result").await

        val expectedEnds = data.dcgEdges.filter(e =>
          sourceHnIds.contains(e.ends.src) &&
            e.isLink &&
            e.linksIds.exists(activeSampleIds.contains)
        ).map(_.ends).toSet

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds

  "DcgGraph.findBackwardThenEdges(...)" should:
    "find backward then edges" in newCase[CaseData]: (tn, data) =>
      val targetHnIds = Set(data.hnId1)

      async[IO]:
        val result: Map[Edge.Ends, DcgEdgeData] = data.graphWithEdges.findBackwardThenEdges(targetHnIds).await
        logInfo(tn, s"result: $result").await

        val expectedEnds = data.dcgEdges.filter(e => targetHnIds.contains(e.ends.trg) && e.isThen).map(_.ends).toSet

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds

    "fail if ends not have as source one of given HnIds" in newCase[CaseData]: (tn, data) =>
      val targetHnIds = Set(data.hnId2) // Not a target for any 'then' edge
      data.graphWithEdges.findBackwardThenEdges(targetHnIds).logValue(tn).assertThrows[AssertionError]

  "DcgGraph.findBackwardActiveThenEdges(...)" should:
    "find backward active then edges" in newCase[CaseData]: (tn, data) =>
      val targetHnIds = Set(data.hnId1)
      val activeSampleIds = Set(data.sampleId2)

      async[IO]:
        val result: Map[Edge.Ends, DcgEdgeData] = data.graphWithEdges
          .findBackwardActiveThenEdges(targetHnIds, activeSampleIds).await

        logInfo(tn, s"result: $result").await

        val expectedEnds = data.dcgEdges.filter(e =>
          targetHnIds.contains(e.ends.trg) &&
            e.isThen &&
            e.thensIds.exists(activeSampleIds.contains)
        ).map(_.ends).toSet

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds

  "DcgGraph.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val reprWithEdges = data.graphWithEdges.repr
        logInfo(tn, s"reprWithEdges: $reprWithEdges").await
        reprWithEdges must include("Concrete Level 0")

  "DcgGraph.empty" should:
    "create empty graph" in newCase[CaseData]: (tn, data) =>
      val emptyGraph = DcgGraph.empty[IO]

      async[IO]:
        logInfo(tn, s"emptyGraph: $emptyGraph").await

        emptyGraph.concreteNodes mustBe Map.empty
        emptyGraph.abstractNodes mustBe Map.empty
        emptyGraph.edgesData mustBe Map.empty
        emptyGraph.edgesMapping mustBe DcgEdgesMapping.empty[IO]
        emptyGraph.samplesData mustBe Map.empty

  "DcgGraph.apply(...)" should:
    "create correct graph from components" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = DcgGraph[IO](data.conNodes, data.absNodes, data.dcgEdges, data.sampleData).await
        logInfo(tn, s"graph: $graph").await

        graph.concreteNodes mustBe data.conNodes.map(n => n.id -> n).toMap
        graph.abstractNodes mustBe data.absNodes.map(n => n.id -> n).toMap
        graph.edgesData mustBe data.dcgEdges.map(e => e.ends -> e).toMap
        graph.edgesMapping mustBe DcgEdgesMapping[IO](data.dcgEdges.map(_.ends))
        graph.samplesData mustBe data.sampleData.map(s => s.id -> s).toMap

    "fail if duplicate concrete node IDs detected" in newCase[CaseData]: (tn, data) =>
      val duplicateConNodes = data.conNodes :+ data.conNodes.head

      DcgGraph[IO](duplicateConNodes, data.absNodes, data.dcgEdges, data.sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Concrete Node IDs detected"))

    "fail if duplicate abstract node IDs detected" in newCase[CaseData]: (tn, data) =>
      val duplicateAbsNodes = data.absNodes :+ data.absNodes.head

      DcgGraph[IO](data.conNodes, duplicateAbsNodes, data.dcgEdges, data.sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Abstract Node IDs detected"))

    "fail if duplicate edge keys detected" in newCase[CaseData]: (tn, data) =>
      val duplicateEdges = data.dcgEdges :+ data.dcgEdges.head

      DcgGraph[IO](data.conNodes, data.absNodes, duplicateEdges, data.sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

    "fail if edges refer to unknown HnIds" in newCase[CaseData]: (tn, data) =>
      val invalidEdges = data.dcgEdges :+ data.makeDcgEdge(data.nuHnId, data.nuHnId, List(data.sampleId1), List())

      DcgGraph[IO](data.conNodes, data.absNodes, invalidEdges, data.sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge refers to unknown HnIds"))

    "fail if some sample IDs used in edges are not found" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.hnId4, data.hnId4, List(data.sampleId1, SampleId(9999)), List())

      DcgGraph[IO](data.conNodes, data.absNodes, data.dcgEdges :+ invalidEdge, data.sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some sample IDs used in edges are not found"))
