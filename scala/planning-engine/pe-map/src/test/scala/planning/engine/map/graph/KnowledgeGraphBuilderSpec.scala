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
| created: 2025-05-10 |||||||||||*/

package planning.engine.map.graph

import cats.effect.{IO, Resource}
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithResource
import planning.engine.map.database.Neo4jDatabaseLike

import cats.effect.cps.*

type TestResource = (Neo4jDatabaseLike[IO], MapBuilder[IO])

class KnowledgeGraphBuilderSpec extends UnitSpecWithResource[TestResource] with AsyncMockFactory
    with KnowledgeGraphTestData:

  override val resource: Resource[IO, TestResource] =
    for
      mockedDb <- Resource.pure(mock[Neo4jDatabaseLike[IO]])
      builder <- MapBuilder[IO](mockedDb)
    yield (mockedDb, builder)

  "KnowledgeGraphBuilder.init()" should:
    "create knowledge graph in given database" in: (mockedDb, builder) =>
      async[IO]:

        mockedDb.initDatabase
          .expects(graphDbData)
          .returns(IO.pure(List(emptyNeo4jNode)))
          .once()

        val graph: KnowledgeGraphLake[IO] = builder.init(testMetadata, List(boolInNode), List(boolOutNode)).await

        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)
        graph.getState.await mustEqual emptyGraphState

  "KnowledgeGraphBuilder.load()" should:
    "load knowledge graph in from database" in: (mockedDb, builder) =>
      async[IO]:

        (() => mockedDb.loadRootNodes)
          .expects()
          .returns(IO.pure(graphDbData))
          .once()

        val graph: KnowledgeGraphLake[IO] = builder.load.await

        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)
        graph.getState.await mustEqual emptyGraphState
