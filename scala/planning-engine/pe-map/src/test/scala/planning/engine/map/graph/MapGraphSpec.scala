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
import planning.engine.map.samples.sample.Sample
import planning.engine.map.subgraph.NextSampleEdgeMap

class MapGraphSpec extends UnitSpecWithData with AsyncMockFactory with MapGraphTestData:

  private class CaseData extends Case:
    lazy val mockedDb = stub[Neo4jDatabaseLike[IO]]
    lazy val mapGraph: MapGraph[IO] = MapGraph[IO]
      .apply(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode), mockedDb)
      .unsafeRunSync()

  "MapGraphSpec.apply(...)" should:
    "crete MapGraph correctly" in newCase[CaseData]: (_, data) =>
      data.mapGraph.pure[IO].asserting: graph =>
        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)

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
              ids = (1L to params.size.toLong).toList.map(HnId.apply)
            yield ids
          .once()

        val nodeIds = data.mapGraph.newConcreteNodes(ConcreteNode.ListNew(newNodes)).await
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))

        nodeIds.size mustEqual newNodes.size
        nodeIds mustEqual expectedHnIds

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
              ids = (1L to params.size.toLong).toList.map(HnId.apply)
            yield ids
          .once()

        val nodeIds = data.mapGraph.newAbstractNodes(AbstractNode.ListNew(newNodes)).await
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))

        nodeIds.size mustEqual newNodes.size
        nodeIds mustEqual expectedHnIds

  "MapGraphSpec.findHiddenNodesByNames(...)" should:
    "find nodes by name" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val newNodes = Set(
          AbstractNode.New(Some(Name("Node1"))),
          AbstractNode.New(Some(Name("Node2"))),
          AbstractNode.New(Some(Name("Node3")))
        )

        val expectedNames = newNodes.map(_.name.getOrElse(fail("Node name should not be empty")))
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))
        val createdNodes = newNodes.zip(expectedHnIds).map((n, id) => AbstractNode[IO](id, n.name))

        data.mockedDb.createAbstractNodes
          .when(*, *)
          .onCall((_, params) => (1L to params.size.toLong).toList.map(HnId.apply).pure)
          .once()

        data.mockedDb.findHiddenNodesByNames
          .when(*, *)
          .onCall: (names, _) =>
            for
              _ <- IO.delay(names mustEqual expectedNames)
              _ <- logInfo(tn, s"Names found: ${names.mkString(", ")}")
            yield createdNodes
              .map(n => (n.name.getOrElse(fail("Node name should not be empty")), Set(n: HiddenNode[IO])))
              .toMap
          .once()

        val foundNodes: Map[Name, Set[HiddenNode[IO]]] = data.mapGraph.findHiddenNodesByNames(expectedNames).await

        foundNodes.size mustEqual newNodes.size
        foundNodes.flatMap((_, ns) => ns.map(_.name)).toSet mustEqual newNodes.map(_.name)
        foundNodes.map((name, _) => name).toSet mustEqual newNodes.map(_.name.get)
        foundNodes.flatMap((_, ns) => ns.map(_.id)).toSet mustEqual expectedHnIds

  "MapGraphSpec.findHnIdsByNames(...)" should:
    "find ids by name" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val testHnIdMap = Map(Name("Node1") -> Set(HnId(1)), Name("Node2") -> Set(HnId(2)))

        data.mockedDb.findHnIdsByNames
          .when(*)
          .onCall: names =>
            for
              _ <- IO.delay(names.toSet mustEqual testHnIdMap.keySet)
              _ <- logInfo(tn, s"Got names = ${names.mkString(", ")}")
            yield testHnIdMap
          .once()

        val foundIds: Map[Name, Set[HnId]] = data.mapGraph.findHnIdsByNames(testHnIdMap.keySet).await
        foundIds mustEqual testHnIdMap

  "MapGraphSpec.countHiddenNodes" should:
    "return total number of hidden nodes" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val testCount: Long = 123
        (() => data.mockedDb.countHiddenNodes).when().returns(testCount.pure[IO]).once()
        val resCount = data.mapGraph.countHiddenNodes.await
        resCount mustEqual testCount

  "MapGraphSpec.addNewSamples(...)" should:
    "addNewSamples samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val testSample = Sample.ListNew(list = Set())
        val expectedSampleIds = List(SampleId(1), SampleId(2))

        data.mockedDb.createSamples
          .when(*)
          .onCall: params =>
            for
              _ <- IO.delay(params mustEqual testSample)
              _ <- logInfo(tn, s"Got params = $params")
            yield (expectedSampleIds, List("edge1", "edge2"))
          .once()

        val resIds: List[SampleId] = data.mapGraph.addNewSamples(testSample).await
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
        val expectedEdges = Set(testNextSampleEdge)

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
