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
| created: 2025-03-15 |||||||||||*/

package planning.engine.map

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.database.Neo4jDatabaseLike
import planning.engine.map.hidden.node.*
import planning.engine.map.samples.sample.{Sample, SampleData}
import planning.engine.map.subgraph.{ConcreteWithParentIds, NextSampleEdgeMap}

class MapGraphSpec extends UnitSpecWithData with AsyncIdiomaticMockito with MapGraphTestData:

  private class CaseData extends Case:
    val testSampleIds = List(SampleId(1), SampleId(2))

    lazy val mockedDb: Neo4jDatabaseLike[IO] = mock[Neo4jDatabaseLike[IO]]
    lazy val mapGraph: MapGraph[IO] = MapGraph[IO]
      .apply(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode), mockedDb)
      .unsafeRunSync()

  "MapGraphSpec.apply(...)" should:
    "crete MapGraph correctly" in newCase[CaseData]: (_, data) =>
      data.mapGraph.pure[IO].asserting: graph =>
        graph.metadata mustEqual testMetadata
        graph.ioNodes mustEqual Map(boolInNode.name -> boolInNode, boolOutNode.name -> boolOutNode)

  "MapGraphSpec.getIoNode(...)" should:
    "get IO node for name" in newCase[CaseData]: (_, data) =>
      data.mapGraph.getIoNode(boolInNode.name).asserting: node =>
        node mustEqual boolInNode

    "fail if IO node not found" in newCase[CaseData]: (_, data) =>
      data.mapGraph.getIoNode(IoName("not_exist_node")).assertThrows[AssertionError]

  "MapGraphSpec.newConcreteNodes(...)" should:
    "add concrete nodes" in newCase[CaseData]: (_, data) =>
      val newNodes = List(
        ConcreteNode.New(HnName.some("inputNode"), None, boolInNode.name, IoIndex(0L)),
        ConcreteNode.New(HnName.some("outputNode"), None, boolOutNode.name, IoIndex(1L)),
      )

      val createdIds = newNodes.zipWithIndex.map((n, i) => HnId(i + 1) -> n.name).toMap
      data.mockedDb.createConcreteNodes(1L, newNodes) returns IO.pure(createdIds)

      async[IO]:
        val nodeIds = data.mapGraph.newConcreteNodes(ConcreteNode.ListNew(newNodes)).await
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1)).toSet

        data.mockedDb.createConcreteNodes(1L, newNodes) was called
        nodeIds.size mustEqual newNodes.size
        nodeIds.keySet mustEqual expectedHnIds

  "MapGraphSpec.newAbstractNodes(...)" should:
    "add abstract nodes" in newCase[CaseData]: (_, data) =>
      val newNodes = List(
        AbstractNode.New(HnName.some("AbstractNode1"), None),
        AbstractNode.New(HnName.some("AbstractNode2"), None),
      )

      val createdIds = newNodes.zipWithIndex.map((n, i) => HnId(i + 1) -> n.name).toMap
      data.mockedDb.createAbstractNodes(1L, newNodes) returns IO.pure(createdIds)

      async[IO]:
        val nodeIds = data.mapGraph.newAbstractNodes(AbstractNode.ListNew(newNodes)).await
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1)).toSet

        data.mockedDb.createAbstractNodes(1L, newNodes) was called
        nodeIds.size mustEqual newNodes.size
        nodeIds.keySet mustEqual expectedHnIds

  "MapGraphSpec.findHiddenNodesByNames(...)" should:
    "find nodes by name" in newCase[CaseData]: (tn, data) =>
      val newNodes = List(
        AbstractNode.New(HnName.some("Node1"), None),
        AbstractNode.New(HnName.some("Node2"), None),
        AbstractNode.New(HnName.some("Node3"), None),
      )

      val expectedNames = newNodes.map(_.name.getOrElse(fail("Node name should not be empty")))
      val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))
      val createdNodes = newNodes.zip(expectedHnIds).map((n, id) => AbstractNode[IO](id, n.name, None))

      val foundByName: Map[HnName, List[HiddenNode[IO]]] = createdNodes
        .map(n => (n.name.getOrElse(fail("Node name should not be empty")), List(n: HiddenNode[IO])))
        .toMap

      data.mockedDb.findHiddenNodesByNames(expectedNames, *) returns IO.pure(foundByName)

      async[IO]:
        val foundNodes: Map[HnName, List[HiddenNode[IO]]] = data.mapGraph.findHiddenNodesByNames(expectedNames).await
        logInfo(tn, s"Names found: ${expectedNames.mkString(", ")}").await

        data.mockedDb.findHiddenNodesByNames(expectedNames, *) was called
        foundNodes.size mustEqual newNodes.size
        foundNodes.flatMap((_, ns) => ns.map(_.name)) mustEqual newNodes.map(_.name)
        foundNodes.map((name, _) => name) mustEqual newNodes.map(_.name.get)
        foundNodes.flatMap((_, ns) => ns.map(_.id)) mustEqual expectedHnIds

  "MapGraphSpec.findHnIdsByNames(...)" should:
    "find ids by name" in newCase[CaseData]: (tn, data) =>
      val testHnIdMap = Map(HnName("Node1") -> List(HnId(1)), HnName("Node2") -> List(HnId(2)))
      val names = testHnIdMap.keys.toList

      data.mockedDb.findHnIdsByNames(names) returns IO.pure(testHnIdMap)

      async[IO]:
        val foundIds: Map[HnName, List[HnId]] = data.mapGraph.findHnIdsByNames(names).await
        logInfo(tn, s"Got names = ${names.mkString(", ")}").await

        data.mockedDb.findHnIdsByNames(names) was called
        foundIds mustEqual testHnIdMap

  "MapGraphSpec.countHiddenNodes" should:
    "return total number of hidden nodes" in newCase[CaseData]: (_, data) =>
      val testCount: Long = 123
      data.mockedDb.countHiddenNodes returns IO.pure(testCount)

      async[IO]:
        val resCount = data.mapGraph.countHiddenNodes.await

        data.mockedDb.countHiddenNodes was called
        resCount mustEqual testCount

  "MapGraphSpec.addNewSamples(...)" should:
    "add new samples" in newCase[CaseData]: (tn, data) =>
      val testSampleList = Sample.ListNew(list = List(newSample))

      val expectedSamples = List(
        testSample.copy(data = testSampleData.copy(id = SampleId(1))),
        testSample.copy(data = testSampleData.copy(id = SampleId(2))),
      )

      data.mockedDb.createSamples(testSampleList) returns IO.pure((expectedSamples, List("edge1", "edge2")))

      async[IO]:
        val resIds: List[Sample] = data.mapGraph.addNewSamples(testSampleList).logValue(tn, "resIds").await

        data.mockedDb.createSamples(testSampleList) was called
        resIds mustEqual expectedSamples

  "MapGraphSpec.countSamples" should:
    "return total number of samples" in newCase[CaseData]: (_, data) =>
      val testCount: Long = 321
      data.mockedDb.countSamples returns IO.pure(testCount)

      async[IO]:
        val resCount = data.mapGraph.countSamples.await

        data.mockedDb.countSamples was called
        resCount mustEqual testCount

  "MapGraphSpec.nextSampleEdges(...)" should:
    "find and return next sample edges" in newCase[CaseData]: (tn, data) =>
      val currentNodeId = HnId(123)
      val expectedEdges = List(testNextSampleEdge)

      data.mockedDb.getNextSampleEdge(currentNodeId, *) returns IO.pure(expectedEdges)

      async[IO]:
        val result: NextSampleEdgeMap[IO] = data.mapGraph.nextSampleEdges(currentNodeId).logValue(tn).await

        data.mockedDb.getNextSampleEdge(currentNodeId, *) was called
        result mustEqual NextSampleEdgeMap(currentNodeId, expectedEdges)

  "MapGraphSpec.getSampleNames(...)" should:
    "get sample names for sample IDs" in newCase[CaseData]: (tn, data) =>
      val expectedSampleNames = data.testSampleIds.zip(List(Name.some("Sample1"), None)).toMap

      data.mockedDb.getSampleNames(data.testSampleIds) returns IO.pure(expectedSampleNames)

      async[IO]:
        val result: Map[SampleId, Option[Name]] = data.mapGraph.getSampleNames(data.testSampleIds).logValue(tn).await

        data.mockedDb.getSampleNames(data.testSampleIds) was called
        result mustEqual expectedSampleNames

  "MapGraphSpec.getSamplesData(...)" should:
    "get sample data for sample IDs" in newCase[CaseData]: (tn, data) =>
      val expectedSampleData = data.testSampleIds.map(id => id -> testSampleData.copy(id = id)).toMap

      data.mockedDb.getSamplesData(data.testSampleIds) returns IO.pure(expectedSampleData)

      async[IO]:
        val result: Map[SampleId, SampleData] = data.mapGraph.getSamplesData(data.testSampleIds).logValue(tn).await

        data.mockedDb.getSamplesData(data.testSampleIds) was called
        result mustEqual expectedSampleData

  "MapGraphSpec.getSamples(...)" should:
    "get samples for sample IDs" in newCase[CaseData]: (tn, data) =>
      val expectedSample = data
        .testSampleIds.map(id => id -> testSample.copy(data = testSample.data.copy(id = id)))
        .toMap

      data.mockedDb.getSamples(data.testSampleIds) returns IO.pure(expectedSample)

      async[IO]:
        val result: Map[SampleId, Sample] = data.mapGraph.getSamples(data.testSampleIds).logValue(tn).await

        data.mockedDb.getSamples(data.testSampleIds) was called
        result mustEqual expectedSample

  "MapGraphSpec.findHiddenNodesByIoValues(...)" should:
    "find hidden nodes connected to particular IO values" in newCase[CaseData]: (tn, data) =>
      val expectedResult = List(ConcreteWithParentIds[IO](testConcreteNode, Set(), Set()))
      val ioNodeWithIndex = List(testConcreteNode.ioNode -> testConcreteNode.valueIndex)

      data.mockedDb.findHiddenNodesByIoValues(ioNodeWithIndex) returns IO.pure(expectedResult)

      async[IO]:
        val result: List[ConcreteWithParentIds[IO]] = data.mapGraph
          .findConcreteNodesByIoValues(Map(testConcreteNode.ioNode.name -> testConcreteNode.valueIndex))
          .logValue(tn)
          .await

        data.mockedDb.findHiddenNodesByIoValues(ioNodeWithIndex) was called
        result mustEqual expectedResult
