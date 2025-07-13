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

package planning.engine.map.graph

import cats.effect.IO
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.map.database.Neo4jDatabaseLike
import cats.syntax.all.*
import planning.engine.common.values.text.Name
import cats.effect.cps.*
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.*
import planning.engine.map.samples.sample.{Sample, SampleData}
import planning.engine.map.subgraph.NextSampleEdgeMap

class MapGraphSpec extends UnitSpecWithData with AsyncMockFactory with MapGraphTestData:

  private class CaseData extends Case:
    val testSampleIds = List(SampleId(1), SampleId(2))

    lazy val mockedDb = stub[Neo4jDatabaseLike[IO]]
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
      data.mapGraph.getIoNode(Name("not_exist_node")).assertThrows[AssertionError]

  "MapGraphSpec.newConcreteNodes(...)" should:
    "add concrete nodes" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val newNodes = List(
          ConcreteNode.New(Some(Name("inputNode")), boolInNode.name, IoIndex(0L)),
          ConcreteNode.New(Some(Name("outputNode")), boolOutNode.name, IoIndex(1L))
        )

        data.mockedDb.createConcreteNodes
          .when(*, *)
          .onCall: (initNextHnIndex, params) =>
            for
              _ <- IO.delay(params.size mustEqual 2L)
              _ <- IO.delay(initNextHnIndex mustEqual 1L)
              ids = params.zipWithIndex.map((p, i) => HnId(i + 1) -> p.name).toMap
            yield ids
          .once()

        val nodeIds = data.mapGraph.newConcreteNodes(ConcreteNode.ListNew(newNodes)).await
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1)).toSet

        nodeIds.size mustEqual newNodes.size
        nodeIds.keySet mustEqual expectedHnIds

  "MapGraphSpec.newAbstractNodes(...)" should:
    "add abstract nodes" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val newNodes = List(
          AbstractNode.New(Some(Name("AbstractNode1"))),
          AbstractNode.New(Some(Name("AbstractNode2")))
        )

        data.mockedDb.createAbstractNodes
          .when(*, *)
          .onCall: (initNextHnIndex, params) =>
            for
              _ <- IO.delay(params.size mustEqual 2L)
              _ <- IO.delay(initNextHnIndex mustEqual 1L)
              ids = params.zipWithIndex.map((p, i) => HnId(i + 1) -> p.name).toMap
            yield ids
          .once()

        val nodeIds = data.mapGraph.newAbstractNodes(AbstractNode.ListNew(newNodes)).await
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1)).toSet

        nodeIds.size mustEqual newNodes.size
        nodeIds.keySet mustEqual expectedHnIds

  "MapGraphSpec.findHiddenNodesByNames(...)" should:
    "find nodes by name" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val newNodes = List(
          AbstractNode.New(Some(Name("Node1"))),
          AbstractNode.New(Some(Name("Node2"))),
          AbstractNode.New(Some(Name("Node3")))
        )

        val expectedNames = newNodes.map(_.name.getOrElse(fail("Node name should not be empty")))
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))
        val createdNodes = newNodes.zip(expectedHnIds).map((n, id) => AbstractNode[IO](id, n.name))

        data.mockedDb.createAbstractNodes
          .when(*, *)
          .onCall((_, params) => params.zipWithIndex.map((p, i) => HnId(i + 1) -> p.name).toMap.pure)
          .once()

        data.mockedDb.findHiddenNodesByNames
          .when(*, *)
          .onCall: (names, _) =>
            for
              _ <- IO.delay(names mustEqual expectedNames)
              _ <- logInfo(tn, s"Names found: ${names.mkString(", ")}")
            yield createdNodes
              .map(n => (n.name.getOrElse(fail("Node name should not be empty")), List(n: HiddenNode[IO])))
              .toMap
          .once()

        val foundNodes: Map[Name, List[HiddenNode[IO]]] = data.mapGraph.findHiddenNodesByNames(expectedNames).await

        foundNodes.size mustEqual newNodes.size
        foundNodes.flatMap((_, ns) => ns.map(_.name)) mustEqual newNodes.map(_.name)
        foundNodes.map((name, _) => name) mustEqual newNodes.map(_.name.get)
        foundNodes.flatMap((_, ns) => ns.map(_.id)) mustEqual expectedHnIds

  "MapGraphSpec.findHnIdsByNames(...)" should:
    "find ids by name" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val testHnIdMap = Map(Name("Node1") -> List(HnId(1)), Name("Node2") -> List(HnId(2)))

        data.mockedDb.findHnIdsByNames
          .when(*)
          .onCall: names =>
            for
              _ <- IO.delay(names.toSet mustEqual testHnIdMap.keySet)
              _ <- logInfo(tn, s"Got names = ${names.mkString(", ")}")
            yield testHnIdMap
          .once()

        val foundIds: Map[Name, List[HnId]] = data.mapGraph.findHnIdsByNames(testHnIdMap.keys.toList).await
        foundIds mustEqual testHnIdMap

  "MapGraphSpec.countHiddenNodes" should:
    "return total number of hidden nodes" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val testCount: Long = 123
        (() => data.mockedDb.countHiddenNodes).when().returns(testCount.pure[IO]).once()
        val resCount = data.mapGraph.countHiddenNodes.await
        resCount mustEqual testCount

  "MapGraphSpec.addNewSamples(...)" should:
    "add new samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val testSample = Sample.ListNew(list = List(newSample))
        val expectedSampleIds = List(SampleId(1), SampleId(2))

        data.mockedDb.createSamples
          .when(*)
          .onCall: params =>
            for
              _ <- IO.delay(params mustEqual testSample)
              _ <- logInfo(tn, s"Got params = $params")
            yield (expectedSampleIds, List("edge1", "edge2"))
          .once()

        val resIds: List[SampleId] = data.mapGraph.addNewSamples(testSample).logValue(tn, "resIds").await
        resIds mustEqual expectedSampleIds

  "MapGraphSpec.countSamples" should:
    "return total number of samples" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val testCount: Long = 321
        (() => data.mockedDb.countSamples).when().returns(testCount.pure[IO]).once()
        val resCount = data.mapGraph.countSamples.await
        resCount mustEqual testCount

  "MapGraphSpec.nextSampleEdges(...)" should:
    "find and return next sample edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val currentNodeId = HnId(123)
        val expectedEdges = List(testNextSampleEdge)

        data.mockedDb.getNextSampleEdge
          .when(*, *)
          .onCall: (curHnId, _) =>
            for
              _ <- IO.delay(curHnId mustEqual currentNodeId)
              _ <- logInfo(tn, s"Got curHnId = $curHnId")
            yield expectedEdges
          .once()

        val result: NextSampleEdgeMap[IO] = data.mapGraph.nextSampleEdges(currentNodeId).await
        result mustEqual NextSampleEdgeMap(currentNodeId, expectedEdges)

  "MapGraphSpec.getSampleNames(...)" should:
    "get sample names for sample IDs" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val expectedSampleNames = data.testSampleIds.zip(List(Some(Name("Sample1")), None)).toMap

        data.mockedDb.getSampleNames
          .when(*)
          .onCall: sampleIds =>
            for
              _ <- IO.delay(sampleIds mustEqual data.testSampleIds)
              _ <- logInfo(tn, s"Got sampleIds = $sampleIds")
            yield expectedSampleNames
          .once()

        val result: Map[SampleId, Option[Name]] = data.mapGraph.getSampleNames(data.testSampleIds).await
        result mustEqual expectedSampleNames

  "MapGraphSpec.getSamplesData(...)" should:
    "get sample data for sample IDs" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val expectedSampleData = data.testSampleIds.map(id => id -> testSampleData.copy(id = id)).toMap

        data.mockedDb.getSamplesData
          .when(*)
          .onCall: sampleIds =>
            for
              _ <- IO.delay(sampleIds mustEqual data.testSampleIds)
              _ <- logInfo(tn, s"Got sampleIds = $sampleIds")
            yield expectedSampleData
          .once()

        val result: Map[SampleId, SampleData] = data.mapGraph.getSamplesData(data.testSampleIds).await
        result mustEqual expectedSampleData

  "MapGraphSpec.getSamples(...)" should:
    "get samples for sample IDs" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val expectedSample = data
          .testSampleIds.map(id => id -> testSample.copy(data = testSample.data.copy(id = id)))
          .toMap

        data.mockedDb.getSamples
          .when(*)
          .onCall: sampleIds =>
            for
              _ <- IO.delay(sampleIds mustEqual data.testSampleIds)
              _ <- logInfo(tn, s"Got sampleIds = $sampleIds")
            yield expectedSample
          .once()

        val result: Map[SampleId, Sample] = data.mapGraph.getSamples(data.testSampleIds).await
        result mustEqual expectedSample
