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

package planning.engine.map.knowledge.graph

import cats.effect.{IO, Resource}
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithResource
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.samples.SamplesState

import cats.effect.cps.*

type TestResource = (Neo4jDatabaseLike[IO], KnowledgeGraphBuilder[IO])

class KnowledgeGraphBuilderSpec extends UnitSpecWithResource[TestResource] with AsyncMockFactory
    with KnowledgeGraphTestData:

  override val resource: Resource[IO, TestResource] =
    for
      mockedDb <- Resource.pure(mock[Neo4jDatabaseLike[IO]])
      builder <- KnowledgeGraphBuilder[IO](mockedDb)
    yield (mockedDb, builder)

  "KnowledgeGraphBuilder.init()" should:
    "create knowledge graph in given database" in: (mockedDb, builder) =>
      async[IO]:

        mockedDb.initDatabase
          .expects(testMetadata, Vector(boolInNode), Vector(boolOutNode), emptySamplesState)
          .returns(IO.pure(Vector(emptyNeo4jNode)))
          .once()

        val graph: KnowledgeGraphLake[IO] = builder.init(testMetadata, Vector(boolInNode), Vector(boolOutNode)).await

        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual Vector(boolInNode)
        graph.outputNodes mustEqual Vector(boolOutNode)

  "KnowledgeGraphBuilder.load()" should:
    "load knowledge graph in from database" in: (mockedDb, builder) =>
      async[IO]:

        (() => mockedDb.loadRootNodes)
          .expects()
          .returns(IO.pure((testMetadata, Vector(boolInNode), Vector(boolOutNode), emptySamplesState)))
          .once()

        val graph: KnowledgeGraphLake[IO] = builder.load.await

        graph.metadata mustEqual testMetadata
        graph.inputNodes mustEqual Vector(boolInNode)
        graph.outputNodes mustEqual Vector(boolOutNode)
