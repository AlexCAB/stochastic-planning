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
| created: 2025-05-11 |||||||||||*/

package planning.engine.integration.tests.map.graph

import cats.effect.{IO, Resource}
import cats.effect.cps.*
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}
import planning.engine.map.database.Neo4jDatabase
import planning.engine.map.graph.{MapBuilder, MapGraphTestData}

class MapGraphBuilderIntegrationSpec extends IntegrationSpecWithResource[(WithItDb.ItDb, MapBuilder[IO])]
    with WithItDb with MapGraphTestData:

  override val resource: Resource[IO, (WithItDb.ItDb, MapBuilder[IO])] =
    for
      itDb <- makeDb()
      neo4jdb <- Neo4jDatabase[IO](itDb.config, itDb.dbName)
      builder <- MapBuilder[IO](neo4jdb)
    yield (itDb, builder)

  "MapGraphBuilder" should:
    "init KnowledgeGraph and load it" in: (_, builder) =>
      async[IO]:
        val createdGraph = builder
          .init(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .logValue("created graph").await

        val loadedGraph = builder.load(testMapConfig).logValue("loaded graph").await

        createdGraph.metadata mustEqual testMetadata
        createdGraph.ioNodes mustEqual boolIoNodes

        createdGraph.metadata mustEqual loadedGraph.metadata
        createdGraph.ioNodes mustEqual loadedGraph.ioNodes
