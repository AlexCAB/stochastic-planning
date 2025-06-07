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
import planning.engine.common.UnitSpecIO
import planning.engine.map.database.Neo4jDatabaseLike
import cats.syntax.all.*
import planning.engine.common.values.text.Name

import scala.collection.immutable.Queue
import cats.effect.cps.*
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.map.hidden.node.ConcreteNode

class MapGraphSpec extends UnitSpecIO with AsyncMockFactory with MapGraphTestData:

  private class CaseData extends Case:
    lazy val mockedDb = mock[Neo4jDatabaseLike[IO]]

    lazy val mapGraph: MapGraph[IO] = MapGraph[IO]
      .apply(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode), emptyGraphState, mockedDb)
      .unsafeRunSync()

  "apply" should:
    "crete MapGraph correctly" in newCase[CaseData]: data =>
      data.mapGraph.pure[IO].asserting: graph =>
        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)

  "getState" should:
    "crete MapGraph correctly" in newCase[CaseData]: data =>
      data.mapGraph.getState.asserting: state =>
        state.hiddenNodes mustEqual Map()
        state.samples     mustEqual Map()
        state.sampleCount mustEqual 0L
        state.hnQueue     mustEqual Queue.empty

  "getIoNode" should:
    "get IO node for name" in newCase[CaseData]: data =>
      data.mapGraph.getIoNode(Name("inputNode")).asserting: node =>
        node mustEqual boolInNode

    "fail if IO node not found" in newCase[CaseData]: data =>
      data.mapGraph.getIoNode(Name("not_exist_node")).assertThrows[AssertionError]

  "newConcreteNodes" should:
    "add concrete nodes" in newCase[CaseData]: data =>
      async[IO]:
        val newNodes = List(
          ConcreteNode.New(Some(Name("Node1")), boolInNode.name, IoIndex(0L)),
          ConcreteNode.New(Some(Name("Node2")), boolOutNode.name, IoIndex(1L))
        )
        
        data.mockedDb
          .createConcreteNodes(newNodes.size, )
          .expects(List(boolInNode, boolOutNode))
          .returns(IO.pure(List.empty))
          .once()
        
        val nodes: List[ConcreteNode[IO]] = data.mapGraph.newConcreteNodes(newNodes).await
        val state: MapCacheState[IO] = data.mapGraph.getState.await
        
        
        
