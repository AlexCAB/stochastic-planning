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

import scala.collection.immutable.Queue
import cats.effect.cps.*
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.map.hidden.node.*

class MapGraphSpec extends UnitSpecWithData with AsyncMockFactory with MapGraphTestData:

  private class CaseData extends Case:
    lazy val mockedDb = stub[Neo4jDatabaseLike[IO]]
    lazy val mapGraph: MapGraph[IO] = MapGraph[IO]
      .apply(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode), emptyGraphState, mockedDb)
      .unsafeRunSync()

  "apply" should:
    "crete MapGraph correctly" in newCase[CaseData]: (_, data) =>
      data.mapGraph.pure[IO].asserting: graph =>
        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)

  "getState" should:
    "crete MapGraph correctly" in newCase[CaseData]: (_, data) =>
      data.mapGraph.getState.asserting: state =>
        state.hiddenNodes mustEqual Map()
        state.samples mustEqual Map()
        state.sampleCount mustEqual 0L
        state.hnQueue mustEqual Queue.empty

  "getIoNode" should:
    "get IO node for name" in newCase[CaseData]: (_, data) =>
      data.mapGraph.getIoNode(boolInNode.name).asserting: node =>
        node mustEqual boolInNode

    "fail if IO node not found" in newCase[CaseData]: (_, data) =>
      data.mapGraph.getIoNode(Name("not_exist_node")).assertThrows[AssertionError]

  "newConcreteNodes" should:
    "add concrete nodes" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val newNodes = List(
          ConcreteNode.New(Some(Name("inputNode")), boolInNode.name, IoIndex(0L)),
          ConcreteNode.New(Some(Name("outputNode")), boolOutNode.name, IoIndex(1L))
        )

        data.mockedDb.createConcreteNodes
          .when(*, *, *)
          .onCall: (numOfNodes, makeNodes, updateCache) =>
            for
              _ <- IO.delay(numOfNodes mustEqual 2L)
              nodes <- makeNodes((1L to newNodes.size.toLong).toList.map(HnId.apply))
              cache <- updateCache(nodes)
            yield (cache, List.empty, nodes)
          .once()

        val nodes = data.mapGraph.newConcreteNodes(newNodes).await
        val state = data.mapGraph.getState.await

        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))

        nodes.size mustEqual newNodes.size
        nodes.map(_.id) mustEqual expectedHnIds
        nodes.map(_.name) mustEqual newNodes.map(_.name)
        nodes.map(_.ioNode.name) mustEqual List(boolInNode.name, boolOutNode.name)
        nodes.map(_.valueIndex) mustEqual newNodes.map(_.valueIndex)

        state.hiddenNodes.keySet mustEqual expectedHnIds.toSet
        state.hnQueue mustEqual expectedHnIds

  "newAbstractNodes" should:
    "add abstract nodes" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val newNodes = List(
          AbstractNode.New(Some(Name("AbstractNode1"))),
          AbstractNode.New(Some(Name("AbstractNode2")))
        )

        data.mockedDb.createAbstractNodes[MapCacheLike[IO]]
          .when(*, *, *)
          .onCall: (numOfNodes, makeNodes, updateCache) =>
            for
              _ <- IO.delay(numOfNodes mustEqual 2L)
              nodes <- makeNodes((1L to newNodes.size.toLong).toList.map(HnId.apply))
              cache <- updateCache(nodes)
            yield (cache, List.empty, nodes)
          .once()

        val nodes = data.mapGraph.newAbstractNodes(newNodes).await
        val state = data.mapGraph.getState.await

        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))

        nodes.size mustEqual newNodes.size
        nodes.map(_.id) mustEqual expectedHnIds
        nodes.map(_.name) mustEqual newNodes.map(_.name)

        state.hiddenNodes.keySet mustEqual expectedHnIds.toSet
        state.hnQueue mustEqual expectedHnIds

  "findHiddenNodesByNames" should:
    "find nodes by name" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val newNodes = List(
          AbstractNode.New(Some(Name("Node1"))),
          AbstractNode.New(Some(Name("Node2"))),
          AbstractNode.New(Some(Name("Node3"))),
          AbstractNode.New(Some(Name("Node4")))
        )

        val expectedNames = newNodes.map(_.name.getOrElse(fail("Node name should not be empty")))
        val expectedHnIds = newNodes.zipWithIndex.map((_, i) => HnId(i + 1))

        data.mockedDb.createAbstractNodes[MapCacheLike[IO]]
          .when(*, *, *)
          .onCall: (numOfNodes, makeNodes, updateCache) =>
            for
              nodes <- makeNodes((1L to numOfNodes).toList.map(HnId.apply))
              cache <- updateCache(nodes)
            yield (cache, List.empty, nodes)
          .once()

        val cachedNodes = data.mapGraph.newAbstractNodes(newNodes.take(2)).await
        val createdNodes = newNodes.drop(2).zipWithIndex.traverse((n, i) => AbstractNode[IO](HnId(i + 3), n.name)).await

        data.mockedDb.findHiddenNodesByNames[MapCacheState[IO]]
          .when(*, *, *, *)
          .onCall: (names, loadCached, _, updateCache) =>
            for
              _ <- IO.delay(names mustEqual expectedNames)
              _ <- logInfo(tn, s"Names found: ${names.mkString(", ")}")
              (loadCache, nodes) <- loadCached(expectedHnIds)
              _ <- logInfo(tn, s"loadCache = $loadCache, nodes = ${nodes.mkString(", ")}")
              _ <- logInfo(tn, s"createdNodes = ${createdNodes.mkString(", ")}")
              _ <- IO.delay(nodes.map(_.id).toSet mustEqual cachedNodes.map(_.id).toSet)
              cache <- updateCache(loadCache, createdNodes)
            yield (cache, nodes ++ createdNodes)
          .once()

        val foundNodes = data.mapGraph.findHiddenNodesByNames(expectedNames).await

        foundNodes.size mustEqual newNodes.size
        foundNodes.map(_.name) mustEqual newNodes.map(_.name)
        foundNodes.map(_.id) mustEqual expectedHnIds
