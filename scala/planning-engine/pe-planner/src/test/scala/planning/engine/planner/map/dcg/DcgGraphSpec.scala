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
import planning.engine.common.graph.GraphStructure
import planning.engine.common.values.node.HnName
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.test.data.DcgSampleTestData
import cats.effect.cps.*
import cats.syntax.all.*
import org.scalatest.compatible.Assertion
import planning.engine.common.UnitSpecWithData
import planning.engine.common.validation.{Validation, ValidationCheck, ValidationError}
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.common.values.node.{MnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.test.data.{DcgNodeTestData, MapSampleTestData, DcgEdgeTestData}
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.common.values.edge.EdgeKey
import planning.engine.common.values.edge.EdgeKey.{Link, Then}

class DcgGraphSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case
      with DcgNodeTestData with DcgEdgeTestData with MapSampleTestData with DcgSampleTestData:

    def makeEdgLink(srcId: MnId, trgId: MnId, ids: Set[SampleId]): DcgEdge[IO] =
      makeDcgEdgeLink(srcId, trgId, makeSampleIds(allHnId, ids.toSeq*))

    def makeEdgThen(srcId: MnId, trgId: MnId, ids: Set[SampleId]): DcgEdge[IO] =
      makeDcgEdgeThen(srcId, trgId, makeSampleIds(allHnId, ids.toSeq*))

    lazy val nuHnId = MnId.Abs(-1)
    lazy val emptyDcgGraph = DcgGraph.empty[IO]
    lazy val allNodes = conNodes ++ absNodes

    lazy val graphWithNodes = emptyDcgGraph
      .copy(nodes = allNodes.map(n => n.id -> n).toMap)

    lazy val lEdge1 = makeEdgLink(hnId1, hnId3, Set(sampleId1, sampleId2))
    lazy val lEdge2 = makeEdgLink(hnId2, hnId4, Set(sampleId1))
    lazy val lEdge3 = makeEdgLink(hnId4, hnId5, Set(sampleId2))
    lazy val lEdge4 = makeEdgLink(hnId2, hnId5, Set(sampleId2))

    lazy val tEdge1 = makeEdgThen(hnId2, hnId1, Set(sampleId2))
    lazy val tEdge2 = makeEdgThen(hnId5, hnId1, Set(sampleId1, sampleId2))

    lazy val dcgEdges: List[DcgEdge[IO]] = List(lEdge1, lEdge2, lEdge3, lEdge4, tEdge1, tEdge2)
    lazy val sampleData: List[SampleData] = List(sampleId1, sampleId2).map(id => makeSampleData(id = id))

    lazy val graphWithEdges: DcgGraph[IO] = graphWithNodes.copy(
      edges = dcgEdges.map(e => e.key -> e).toMap,
      samples = sampleData.map(s => s.id -> s).toMap,
      structure = GraphStructure[IO](dcgEdges.map(_.key).toSet)
    )

  "DcgGraph.mnIds" should:
    "return all MnIds" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.mnIds.pure[IO].asserting(_ mustBe data.allHnId)

  "DcgGraph.allSampleIds" should:
    "return all SampleIds" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.sampleIds.pure[IO].asserting(_ mustBe Set(data.sampleId1, data.sampleId2))

  "DcgGraph.conMnId" should:
    "return all concrete MnIds" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.conMnId.pure[IO].asserting(_ mustBe data.allConMnId)

  "DcgGraph.absMnId" should:
    "return all abstract MnIds" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.absMnId.pure[IO].asserting(_ mustBe data.allAbsMnId)

  "DcgGraph.isEmpty" should:
    "return true for empty graph" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.pure[IO].asserting(_.isEmpty mustBe true)

    "return false for non empty graph" in newCase[CaseData]: (tn, data) =>
      data.graphWithNodes.pure[IO].asserting(_.isEmpty mustBe false)

  "DcgGraph.allIndexies" should:
    "return all HnIndexies" in newCase[CaseData]: (tn, data) =>
      val allIndexiesMap: Map[MnId, Set[HnIndex]] = data.dcgEdges
        .flatMap(e => Set(e.key.src -> e.samples.srcHnIndex, e.key.trg -> e.samples.trgHnIndex))
        .groupBy(_._1).map((mnId, lst) => mnId -> lst.flatMap(_._2).toSet)

      data.graphWithEdges.allIndexies.pure[IO].logValue(tn).asserting: allIndexies =>
        allIndexies must not be empty
        allIndexies.keySet mustBe data.graphWithEdges.mnIds
        allIndexies mustBe allIndexiesMap

  "DcgGraph.validationName" should:
    "return correct name" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgGraph.checkValidationName("DcgGraph", tn)

  "DcgGraph.validationErrors" should:
    "return no errors for valid graph" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges.checkNoValidationError(tn)

    "return error when concrete nodes map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      data
        .graphWithEdges.copy(nodes = data.graphWithEdges.nodes + (data.nuHnId -> data.conNodes.head))
        .checkOneValidationError("Nodes map keys and values IDs mismatch", tn)

    "return error when edges data map keys and values mismatch" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges
        .copy(edges = data.graphWithEdges.edges + (EdgeKey.Link(data.hnId3, data.hnId3) -> data.lEdge1))
        .checkOneOfValidationErrors("Edges data map keys and values key mismatch", tn)

    "return error when samples data map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      data.graphWithEdges
        .copy(samples = data.graphWithEdges.samples + (data.sampleId1 -> data.makeSampleData(SampleId(9999))))
        .checkOneValidationError("Samples data map keys and values IDs mismatch", tn)

    "return error when edge refers to unknown MnIds" in newCase[CaseData]: (tn, data) =>
      import data.{graphWithEdges, allHnId, nuHnId, sampleId1, makeDcgEdgeLink, makeSampleIds}
      val invalidEdge = makeDcgEdgeLink(nuHnId, nuHnId, makeSampleIds(allHnId + nuHnId, sampleId1))

      graphWithEdges
        .copy(edges = graphWithEdges.edges + (invalidEdge.key -> invalidEdge))
        .checkOneOfValidationErrors("Edge refers to unknown MnIds", tn)

    "return error when edges mapping refers to unknown HnIds and unknown edge ends" in newCase[CaseData]: (tn, data) =>
      val invalidGraph = data.graphWithEdges.copy(
        structure = data.graphWithEdges.structure.copy(
          srcMap = data.graphWithEdges.structure.srcMap + (data.nuHnId -> Set(EdgeKey.Link.End(data.hnId1))),
          trgMap = data.graphWithEdges.structure.trgMap + (data.hnId1 -> Set(EdgeKey.Link.End(data.nuHnId)))
        )
      )

      invalidGraph.checkOneOfValidationErrors("Graph structure refers to unknown MnIds", tn)

    "return error when graph structure refers to unknown edge key" in newCase[CaseData]: (tn, data) =>
      val invalidGraph = data.graphWithEdges.copy(
        structure = data.graphWithEdges.structure
          .copy(keys = data.graphWithEdges.structure.keys + EdgeKey.Link(data.hnId1, data.hnId5))
      )

      invalidGraph.checkOneOfValidationErrors("Graph structure refers to unknown edge key", tn)

    "return error when some sample IDs used in edges are not found" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeEdgLink(data.hnId1, data.hnId3, Set(data.sampleId1, SampleId(9999)))

      data.graphWithEdges
        .copy(edges = data.graphWithEdges.edges + (invalidEdge.key -> invalidEdge))
        .checkOneValidationError("Some sample IDs used in edges are not found", tn)

  "DcgGraph.getNodes(...)" should:
    "get concrete node for MnId" in newCase[CaseData]: (tn, data) =>
      import data.{conNodes, graphWithNodes}
      val testConNodes = conNodes.take(2)

      graphWithNodes.getNodes[DcgNode.Concrete[IO]](testConNodes.map(_.id).toSet).logValue(tn)
        .asserting(_ mustBe testConNodes.map(n => n.id -> n).toMap)

    "get abstract node for MnId" in newCase[CaseData]: (tn, data) =>
      import data.{absNodes, graphWithNodes}
      val testAbsNodes = absNodes.take(2)

      graphWithNodes.getNodes[DcgNode.Abstract[IO]](testAbsNodes.map(_.id).toSet).logValue(tn)
        .asserting(_ mustBe testAbsNodes.map(n => n.id -> n).toMap)

    "fail if node for HnId not found" in newCase[CaseData]: (tn, data) =>
      import data.{nuHnId, graphWithNodes}

      graphWithNodes.getNodes[DcgNode.Concrete[IO]](Set(nuHnId)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Some node IDs are not found"))

  "DcgGraph.getEdges(...)" should:
    "get LINK edges correctly" in newCase[CaseData]: (tn, data) =>
      import data.{graphWithEdges, lEdge1, lEdge2}
      val edges = Set(lEdge1, lEdge2).map(e => e.key -> e).toMap
      graphWithEdges.getEdges[EdgeKey.Link](edges.keySet).logValue(tn).asserting(_ mustBe edges)

    "get THEN edges correctly" in newCase[CaseData]: (tn, data) =>
      import data.{graphWithEdges, tEdge1, tEdge2}
      val edges = Set(tEdge1, tEdge2).map(e => e.key -> e).toMap
      graphWithEdges.getEdges[EdgeKey.Then](edges.keySet).logValue(tn).asserting(_ mustBe edges)

    "fail if edge for key not found" in newCase[CaseData]: (tn, data) =>
      import data.{nuHnId, graphWithEdges}

      graphWithEdges.getEdges[EdgeKey.Link](Set(EdgeKey.Link(nuHnId, nuHnId))).logValue(tn)
        .assertThrowsError(_.getMessage must include("Some edge keys are not found"))

  "DcgGraph.getSamples(...)" should:
    "find samples" in newCase[CaseData]: (tn, data) =>
      import data.{sampleData, graphWithEdges}
      val sample = sampleData.head
      graphWithEdges.getSamples(Set(sample.id)).logValue(tn).asserting(_ mustBe Map(sample.id -> sample))

    "fail if sample ID not found" in newCase[CaseData]: (tn, data) =>
      import data.graphWithEdges

      graphWithEdges.getSamples(Set(SampleId(9999))).logValue(tn)
        .assertThrowsError(_.getMessage must include("Some sample IDs are not found"))

  "DcgGraph.addNodes(...)" should:
    "add nodes to graph" in newCase[CaseData]: (tn, data) =>
      import data.{graphWithNodes, makeConDcgNode}
      val newIds = List(MnId.Con(9998), MnId.Con(9999))
      val newNodes = newIds.map(newId => makeConDcgNode(newId))
      val newNodeMap = newNodes.map(n => n.id -> n).toMap

      graphWithNodes
        .addNodes(newNodes.map(_.asDcgNode)).logValue(tn)
        .asserting(_.nodes.filter((id, _) => newIds.map(_.asMnId).contains(id)) mustBe newNodeMap)

    "fail if duplicate node IDs" in newCase[CaseData]: (tn, data) =>
      import data.{makeConDcgNode, graphWithNodes}
      val newNode = makeConDcgNode(MnId.Con(9998))
      val duplicateNodes = List(newNode, newNode.copy(name = HnName.some("Duplicate Node")))

      graphWithNodes.addNodes(duplicateNodes.map(_.asDcgNode)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Node IDs detected"))

    "fail if node ID already exists in graph" in newCase[CaseData]: (tn, data) =>
      import data.{conNodes, graphWithNodes}
      val existingNode = conNodes.head

      graphWithNodes.addNodes(List(existingNode)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Can't add nodes that already exist"))

  "DcgGraph.addSamples(...)" should:
    def checkSampleAdded(graph: DcgGraph[IO], add: DcgSample.Add[IO], data: CaseData, tn: String): IO[Assertion] =
      import data.*
      async[IO]:
        val newSampleId = add.sample.data.id

        val upGraph: DcgGraph[IO] = graph.addSamples(List(add)).await
        Validation.validate[IO](upGraph).await

        val upEdges: Map[EdgeKey, DcgEdge[IO]] = upGraph.getEdges[EdgeKey](add.sample.structure.keys).await
        logInfo(tn, s"upEdges:\n${upEdges.values.mkString("\n")}").await

        upEdges.keySet mustBe add.sample.structure.keys

        upEdges.foreach: (key, edge) =>
          edge.samples.sampleIds must contain(newSampleId)
          edge.samples.getSrcIndex(newSampleId) mustBe add.indexMap.indexies(key.src)
          edge.samples.getTrgIndex(newSampleId) mustBe add.indexMap.indexies(key.trg)

        succeed

    "add samples with exist edges" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, hnId2, hnId4, hnId5, hnId1}
      val addWithExistEdges = makeDcgSampleAdd(SampleId(1))(Link(hnId2, hnId4), Link(hnId4, hnId5), Then(hnId2, hnId1))
      checkSampleAdded(data.graphWithEdges, addWithExistEdges, data, tn)

    "add samples with new edges" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, hnId1, hnId2, hnId3}
      val addWithNewEdges = makeDcgSampleAdd(SampleId(2))(Link(hnId1, hnId2), Link(hnId2, hnId3), Then(hnId3, hnId1))
      checkSampleAdded(data.graphWithEdges, addWithNewEdges, data, tn)

    "fail if sample is invalid" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, hnId1, hnId2, hnId3, hnId4}
      val invalidSampleAdd = makeDcgSampleAdd(SampleId(3))(Link(hnId1, hnId2), Link(hnId3, hnId4))

      data.graphWithEdges.addSamples(List(invalidSampleAdd)).logValue(tn)
        .assertThrowsError[ValidationError](_.getMessage must include("DcgSample edges must form a connected graph"))

    "fail if duplicate sample IDs detected" in newCase[CaseData]: (tn, data) =>
      import data.{graphWithEdges, makeDcgSampleAdd, hnId1, hnId2}
      val sampleAdd = makeDcgSampleAdd(SampleId(4))(Link(hnId1, hnId2))

      graphWithEdges.addSamples(List(sampleAdd, sampleAdd)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate sample IDs detected"))

    "fail if some sample IDs already exists in the graph" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, graphWithEdges, sampleId1, hnId1, hnId2}
      val sampleAdd = makeDcgSampleAdd(sampleId1)(Link(hnId1, hnId2))

      graphWithEdges.addSamples(List(sampleAdd)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some sample IDs already exists in the graph"))

  "DcgGraph.findHnIdsByNames(...)" should:
    "find HnIds by names" in newCase[CaseData]: (tn, data) =>
      val name1 = data.absNodes.head.name.get
      val name2 = HnName("unknown_name")

      async[IO]:
        val result = data.graphWithNodes.findHnIdsByNames(Set(name1, name2))
        logInfo(tn, s"result: $result").await

        result mustBe Map(name1 -> data.absNodes.filter(_.name.contains(name1)).map(_.id).toSet)

  "DcgGraph.findForwardLinkEdges(...)" should:
    "find forward link edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val sourceHnIds = Set[MnId](data.hnId1, data.hnId2)

        val result: Map[EdgeKey.Link, DcgEdge[IO]] = data.graphWithEdges.findForwardLinkEdges(sourceHnIds).await
        logInfo(tn, s"result:\n${result.values.mkString("\n")}").await

        val expectedEnds = data.dcgEdges.filter(e => sourceHnIds.contains(e.key.src) && e.key.isLink).map(_.key)

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds.toSet

  "DcgGraph.findForwardActiveLinkEdges(...)" should:
    "find forward active link edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val sourceHnIds = Set[MnId](data.hnId1, data.hnId2)
        val activeSampleIds = Set(data.sampleId1)

        val result: Map[EdgeKey.Link, DcgEdge[IO]] = data.graphWithEdges
          .findForwardActiveLinkEdges(sourceHnIds, activeSampleIds).await

        logInfo(tn, s"result:\n${result.values.mkString("\n")}").await

        val expectedEnds = data.dcgEdges
          .filter(e =>
            sourceHnIds.contains(e.key.src)
              && e.key.isLink
              && e.samples.sampleIds.exists(activeSampleIds.contains)
          )
          .map(_.key)

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds.toSet

  "DcgGraph.findBackwardThenEdges(...)" should:
    "find backward then edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val targetHnIds = Set[MnId](data.hnId1)

        val result: Map[EdgeKey.Then, DcgEdge[IO]] = data.graphWithEdges.findBackwardThenEdges(targetHnIds).await
        logInfo(tn, s"result:\n${result.values.mkString("\n")}").await

        val expectedEnds = data.dcgEdges.filter(e => targetHnIds.contains(e.key.trg) && e.key.isThen).map(_.key)

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds.toSet

  "DcgGraph.findBackwardActiveThenEdges(...)" should:
    "find backward active then edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val targetHnIds = Set[MnId](data.hnId1)
        val activeSampleIds = Set(data.sampleId2)

        val result: Map[EdgeKey.Then, DcgEdge[IO]] = data.graphWithEdges
          .findBackwardActiveThenEdges(targetHnIds, activeSampleIds).await

        logInfo(tn, s"result:\n${result.values.mkString("\n")}").await

        val expectedEnds = data.dcgEdges
          .filter(e =>
            targetHnIds.contains(e.key.trg) &&
              e.key.isThen &&
              e.samples.sampleIds.exists(activeSampleIds.contains)
          )
          .map(_.key)

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds.toSet

  "DcgGraph.empty" should:
    "create empty graph" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val emptyGraph = DcgGraph.empty[IO]
        logInfo(tn, s"emptyGraph: $emptyGraph").await

        emptyGraph.nodes mustBe Map.empty
        emptyGraph.edges mustBe Map.empty
        emptyGraph.samples mustBe Map.empty
        emptyGraph.structure.keys mustBe Set.empty

  "DcgGraph.apply(...)" should:
    "create correct graph from components" in newCase[CaseData]: (tn, data) =>
      import data.{allNodes, dcgEdges, sampleData}
      async[IO]:
        val graph = DcgGraph[IO](allNodes, dcgEdges, sampleData).await
        logInfo(tn, s"graph: $graph").await

        graph.nodes mustBe allNodes.map(n => n.id -> n).toMap
        graph.edges mustBe dcgEdges.map(e => e.key -> e).toMap
        graph.samples mustBe sampleData.map(s => s.id -> s).toMap
        graph.structure.keys mustBe dcgEdges.map(_.key).toSet

    "fail if duplicate node IDs detected" in newCase[CaseData]: (tn, data) =>
      import data.{allNodes, dcgEdges, sampleData}
      val duplicateNodes = allNodes :+ allNodes.head

      DcgGraph[IO](duplicateNodes, dcgEdges, sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate node IDs detected"))

    "fail if duplicate edge keys detected" in newCase[CaseData]: (tn, data) =>
      import data.{allNodes, dcgEdges, sampleData}
      val duplicateEdges = dcgEdges :+ dcgEdges.head

      DcgGraph[IO](allNodes, duplicateEdges, sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

    "fail if edges refer to unknown MnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidEdges = dcgEdges :+ makeDcgEdgeLink(nuHnId, nuHnId, makeSampleIds(allHnId + nuHnId, sampleId1))

      DcgGraph[IO](allNodes, invalidEdges, sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge refers to unknown HnIds"))

    "fail if some sample IDs used in edges are not found" in newCase[CaseData]: (tn, data) =>
      import data.{allNodes, dcgEdges, sampleData, makeEdgLink, hnId4, sampleId1}
      val invalidEdge = makeEdgLink(hnId4, hnId4, Set(sampleId1, SampleId(9999)))

      DcgGraph[IO](allNodes, dcgEdges :+ invalidEdge, sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some sample IDs used in edges are not found"))
