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

package planning.engine.integration.tests.knowledge.graph

import cats.effect.{IO, Resource}
import cats.effect.cps.*
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}
import planning.engine.map.database.Neo4jDatabase
import planning.engine.map.graph.{MapBuilder, KnowledgeGraphTestData}

type TestResource = (WithItDb.ItDb, MapBuilder[IO])

class KnowledgeGraphBuilderIntegrationSpec extends IntegrationSpecWithResource[TestResource] with WithItDb
    with KnowledgeGraphTestData:

  override val resource: Resource[IO, TestResource] =
    for
      itDb <- makeDb
      neo4jdb <- Neo4jDatabase[IO](itDb.config, itDb.dbName)
      builder <- MapBuilder[IO](neo4jdb)
    yield (itDb, builder)

  "KnowledgeGraphBuilder" should:
    "init KnowledgeGraph and load it" in: (_, builder) =>
      async[IO]:
        val createdGraph = builder.init(testMetadata, List(boolInNode), List(boolOutNode)).logValue.await
        val loadedGraph = builder.load.logValue.await

        createdGraph.metadata mustEqual testMetadata
        createdGraph.inputNodes mustEqual List(boolInNode)
        createdGraph.outputNodes mustEqual List(boolOutNode)
        createdGraph.samples.getState.await mustEqual emptySamplesState

        createdGraph.metadata mustEqual loadedGraph.metadata
        createdGraph.inputNodes mustEqual loadedGraph.inputNodes
        createdGraph.outputNodes mustEqual loadedGraph.outputNodes
        createdGraph.samples.getState.await mustEqual loadedGraph.samples.getState.await
