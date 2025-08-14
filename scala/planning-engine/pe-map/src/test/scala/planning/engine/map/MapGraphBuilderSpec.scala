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

package planning.engine.map

import cats.effect.cps.*
import cats.effect.{IO, Resource}
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithResource
import planning.engine.common.values.db.DbName
import planning.engine.database.Neo4jDatabaseLike

class MapGraphBuilderSpec
    extends UnitSpecWithResource[(Neo4jDatabaseLike[IO], DbName => IO[Neo4jDatabaseLike[IO]], MapBuilder[IO])]
    with AsyncMockFactory with MapGraphTestData:

  override val resource: Resource[IO, (Neo4jDatabaseLike[IO], DbName => IO[Neo4jDatabaseLike[IO]], MapBuilder[IO])] =
    for
      mockedDb <- Resource.pure(mock[Neo4jDatabaseLike[IO]])
      mockedMakeDb <- Resource.pure(mock[DbName => IO[Neo4jDatabaseLike[IO]]])
      builder <- Resource.pure(new MapBuilder[IO](mockedMakeDb))
    yield (mockedDb, mockedMakeDb, builder)

  "MapGraphBuilder.init(...)" should:
    "create map graph in given database" in: (mockedDb, mockedMakeDb, builder) =>
      async[IO]:
        mockedMakeDb.apply.expects(testDbName).returns(IO.pure(mockedDb)).once()

        mockedDb.initDatabase
          .expects(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .returns(IO.pure(List(emptyNeo4jNode)))
          .once()

        val graph: MapGraphLake[IO] = builder
          .init(testDbName, testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .await

        graph.metadata mustEqual testMetadata
        graph.ioNodes mustEqual Map(boolInNode.name -> boolInNode, boolOutNode.name -> boolOutNode)

  "MapGraphBuilder.load(...)" should:
    "load map graph in from database" in: (mockedDb, mockedMakeDb, builder) =>
      async[IO]:
        mockedMakeDb.apply.expects(testDbName).returns(IO.pure(mockedDb)).once()

        (() => mockedDb.loadRootNodes)
          .expects()
          .returns(IO.pure((testMetadata, List(boolInNode), List(boolOutNode))))
          .once()

        val graph: MapGraphLake[IO] = builder.load(testDbName, testMapConfig).logValue("load", "graph").await

        graph.metadata mustEqual testMetadata
        graph.ioNodes mustEqual Map(boolInNode.name -> boolInNode, boolOutNode.name -> boolOutNode)
