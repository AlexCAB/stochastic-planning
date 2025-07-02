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
import planning.engine.map.hidden.node.*

class MapGraphSpec extends UnitSpecWithData with AsyncMockFactory with MapGraphTestData:

  private class CaseData extends Case:
    lazy val mockedDb = stub[Neo4jDatabaseLike[IO]]
    lazy val mapGraph: MapGraph[IO] = MapGraph[IO]
      .apply(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode), mockedDb)
      .unsafeRunSync()

  "apply" should:
    "crete MapGraph correctly" in newCase[CaseData]: (_, data) =>
      data.mapGraph.pure[IO].asserting: graph =>
        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)

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

  "newAbstractNodes" should:
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

  "findHiddenNodesByNames" should:
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
          .onCall((_, params) => (1L to params.size.toLong).toList.map(HnId.apply).pure)
          .once()

        data.mockedDb.findHiddenNodesByNames
          .when(*, *)
          .onCall: (names, _) =>
            for
              _ <- IO.delay(names mustEqual expectedNames)
              _ <- logInfo(tn, s"Names found: ${names.mkString(", ")}")
            yield createdNodes
          .once()

        val foundNodes = data.mapGraph.findHiddenNodesByNames(expectedNames).await

        foundNodes.size mustEqual newNodes.size
        foundNodes.map(_.name) mustEqual newNodes.map(_.name)
        foundNodes.map(_.id) mustEqual expectedHnIds
