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

class MapGraphBuilderSpec extends UnitSpecWithResource[(Neo4jDatabaseLike[IO], MapBuilder[IO])] with AsyncMockFactory
    with MapGraphTestData:

  override val resource: Resource[IO, (Neo4jDatabaseLike[IO], MapBuilder[IO])] =
    for
      mockedDb <- Resource.pure(mock[Neo4jDatabaseLike[IO]])
      builder <- MapBuilder[IO](mockedDb)
    yield (mockedDb, builder)

  "KnowledgeGraphBuilder.init(...)" should:
    "create knowledge graph in given database" in: (mockedDb, builder) =>
      async[IO]:

        mockedDb.initDatabase
          .expects(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .returns(IO.pure(List(emptyNeo4jNode)))
          .once()

        val graph: MapGraphLake[IO] = builder
          .init(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .await

        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)

  "KnowledgeGraphBuilder.load(...)" should:
    "load knowledge graph in from database" in: (mockedDb, builder) =>
      async[IO]:

        (() => mockedDb.loadRootNodes)
          .expects()
          .returns(IO.pure((testMetadata, List(boolInNode), List(boolOutNode))))
          .once()

        val graph: MapGraphLake[IO] = builder.load(testMapConfig).await

        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual List(boolInNode)
        graph.outputNodes mustEqual List(boolOutNode)
