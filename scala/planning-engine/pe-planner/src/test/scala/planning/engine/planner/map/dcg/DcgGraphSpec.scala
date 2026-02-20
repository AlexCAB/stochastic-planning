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
import planning.engine.planner.map.test.data.DcgGraphTestData
import cats.effect.cps.*
import cats.syntax.all.*
import org.scalatest.compatible.Assertion
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.common.values.edge.EdgeKey
import planning.engine.common.values.edge.EdgeKey.{Link, Then}

class DcgGraphSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgGraphTestData:
    val nodesMap = allNodes.map(n => n.id -> n).toMap
    val edgesMap = dcgEdges.map(e => e.key -> e).toMap
    val samplesMap = sampleData.map(s => s.id -> s).toMap
    val structureMap = GraphStructure[IO](dcgEdges.map(_.key).toSet)

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
        val upEdges: Map[EdgeKey, DcgEdge[IO]] = upGraph.getEdges[EdgeKey](add.sample.structure.keys).await

        logInfo(tn, s"upEdges:\n${upEdges.values.mkString("\n")}").await
        upEdges.keySet mustBe add.sample.structure.keys

        upEdges.foreach: (key, edge) =>
          edge.samples.sampleIds must contain(newSampleId)
          edge.samples.getSrcIndex(newSampleId) mustBe add.indexMap.indexies(key.src)
          edge.samples.getTrgIndex(newSampleId) mustBe add.indexMap.indexies(key.trg)

        succeed

    "add samples with exist edges" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, mnId2, mnId4, mnId5, mnId1}
      val addWithExistEdges = makeDcgSampleAdd(SampleId(1))(Link(mnId2, mnId4), Link(mnId4, mnId5), Then(mnId2, mnId1))
      checkSampleAdded(data.graphWithEdges, addWithExistEdges, data, tn)

    "add samples with new edges" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, mnId1, mnId2, mnId3}
      val addWithNewEdges = makeDcgSampleAdd(SampleId(2))(Link(mnId1, mnId2), Link(mnId2, mnId3), Then(mnId3, mnId1))
      checkSampleAdded(data.graphWithEdges, addWithNewEdges, data, tn)

    "fail if duplicate sample IDs detected" in newCase[CaseData]: (tn, data) =>
      import data.{graphWithEdges, makeDcgSampleAdd, mnId1, mnId2}
      val sampleAdd = makeDcgSampleAdd(SampleId(4))(Link(mnId1, mnId2))

      graphWithEdges.addSamples(List(sampleAdd, sampleAdd)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate sample IDs detected"))

    "fail if some sample IDs already exists in the graph" in newCase[CaseData]: (tn, data) =>
      import data.{makeDcgSampleAdd, graphWithEdges, sampleId1, mnId1, mnId2}
      val sampleAdd = makeDcgSampleAdd(sampleId1)(Link(mnId1, mnId2))

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
        val sourceHnIds = Set[MnId](data.mnId1, data.mnId2)

        val result: Map[EdgeKey.Link, DcgEdge[IO]] = data.graphWithEdges.findForwardLinkEdges(sourceHnIds).await
        logInfo(tn, s"result:\n${result.values.mkString("\n")}").await

        val expectedEnds = data.dcgEdges.filter(e => sourceHnIds.contains(e.key.src) && e.key.isLink).map(_.key)

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds.toSet

  "DcgGraph.findForwardActiveLinkEdges(...)" should:
    "find forward active link edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val sourceHnIds = Set[MnId](data.mnId1, data.mnId2)
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
        val targetHnIds = Set[MnId](data.mnId1)

        val result: Map[EdgeKey.Then, DcgEdge[IO]] = data.graphWithEdges.findBackwardThenEdges(targetHnIds).await
        logInfo(tn, s"result:\n${result.values.mkString("\n")}").await

        val expectedEnds = data.dcgEdges.filter(e => targetHnIds.contains(e.key.trg) && e.key.isThen).map(_.key)

        expectedEnds must not be empty
        result.keySet mustBe expectedEnds.toSet

  "DcgGraph.findBackwardActiveThenEdges(...)" should:
    "find backward active then edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val targetHnIds = Set[MnId](data.mnId1)
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

  "DcgGraph.apply(nodes, edges, samples)" should:
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
      import data.{allNodes, dcgEdges, sampleData, makeEdgLink, mnId4, sampleId1}
      val invalidEdge = makeEdgLink(mnId4, mnId4, Set(sampleId1, SampleId(9999)))

      DcgGraph[IO](allNodes, dcgEdges :+ invalidEdge, sampleData)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some sample IDs used in edges are not found"))

  "DcgGraph.apply(nodes, edges, samples, structure)" should:
    "create graph from valid data" in newCase[CaseData]: (tn, data) =>
      import data.*
      DcgGraph[IO](nodesMap, edgesMap, samplesMap, structureMap).logValue(tn).asserting(_ mustBe graphWithEdges)

    "return error when concrete nodes map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidNodes = nodesMap + (nuHnId -> conNodes.head)

      DcgGraph[IO](invalidNodes, edgesMap, samplesMap, structureMap).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Nodes map keys and values IDs mismatch"))

    "return error when edges data map keys and values mismatch" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidEdges = edgesMap + (EdgeKey.Link(mnId3, mnId3) -> lEdge1)

      DcgGraph[IO](nodesMap, invalidEdges, samplesMap, structureMap).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edges data map keys and values key mismatch"))

    "return error when samples data map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidSamples = samplesMap + (sampleId1 -> makeDcgSampleData(SampleId(9999)))

      DcgGraph[IO](nodesMap, edgesMap, invalidSamples, structureMap).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Samples data map keys and values IDs mismatch"))

    "return error when edge refers to unknown MnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidEdge = makeDcgEdgeLink(nuHnId, nuHnId, makeSampleIds(allHnId + nuHnId, sampleId1))

      DcgGraph[IO](nodesMap, edgesMap + (invalidEdge.key -> invalidEdge), samplesMap, structureMap).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge refers to unknown MnIds"))

    "return error when edges mapping refers to unknown HnIds and unknown edge ends" in newCase[CaseData]: (tn, data) =>
      import data.*
      val srcMap = graphWithEdges.structure.srcMap + (nuHnId -> Set(EdgeKey.Link.End(mnId1)))
      val trgMap = graphWithEdges.structure.trgMap + (mnId1 -> Set(EdgeKey.Link.End(nuHnId)))
      val invalidStructure = graphWithEdges.structure.copy[IO](srcMap = srcMap, trgMap = trgMap)

      DcgGraph[IO](nodesMap, edgesMap, samplesMap, invalidStructure).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Graph structure refers to unknown MnIds"))

    "return error when graph structure refers to unknown edge key" in newCase[CaseData]: (tn, data) =>
      import data.*
      val keys = graphWithEdges.structure.keys + EdgeKey.Link(mnId1, mnId5)
      val invalidStructure = graphWithEdges.structure.copy[IO](keys = keys)

      DcgGraph[IO](nodesMap, edgesMap, samplesMap, invalidStructure).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Graph structure refers to unknown edge key"))

    "return error when some sample IDs used in edges are not found" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidEdge = data.makeEdgLink(data.mnId1, data.mnId3, Set(data.sampleId1, SampleId(9999)))

      DcgGraph[IO](nodesMap, edgesMap + (invalidEdge.key -> invalidEdge), samplesMap, structureMap).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Some sample IDs used in edges are not found"))
