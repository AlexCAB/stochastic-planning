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
import planning.engine.common.{MockitoWithResource, UnitSpecWithResource}
import planning.engine.common.values.db.DbName
import planning.engine.database.Neo4jDatabaseLike

class MapGraphBuilderSpec
    extends UnitSpecWithResource[(Neo4jDatabaseLike[IO], DbName => IO[Neo4jDatabaseLike[IO]], MapBuilder[IO])]
    with MockitoWithResource with MapGraphTestData:

  override val resource: Resource[IO, (Neo4jDatabaseLike[IO], DbName => IO[Neo4jDatabaseLike[IO]], MapBuilder[IO])] =
    for
      mockedDb <- Resource.pure(mock[Neo4jDatabaseLike[IO]])
      mockedMakeDb <- Resource.pure(mock[DbName => IO[Neo4jDatabaseLike[IO]]])
      builder <- Resource.pure(new MapBuilder[IO](mockedMakeDb))
    yield (mockedDb, mockedMakeDb, builder)

  "MapGraphBuilder.init(...)" should:
    "create map graph in given database" in: (mockedDb, mockedMakeDb, builder) =>
      reset(mockedDb, mockedMakeDb) // the mocks are shared between the tests of this spec

      mockedMakeDb(testDbName) returns IO.pure(mockedDb)

      mockedDb.initDatabase(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode)) returns
        IO.pure(List(emptyNeo4jNode))

      async[IO]:
        val graph: MapGraphLake[IO] = builder
          .init(testDbName, testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .await

        mockedMakeDb(testDbName) was called
        mockedDb.initDatabase(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode)) was called

        graph.metadata mustEqual testMetadata
        graph.ioNodes mustEqual Map(boolInNode.name -> boolInNode, boolOutNode.name -> boolOutNode)

  "MapGraphBuilder.load(...)" should:
    "load map graph in from database" in: (mockedDb, mockedMakeDb, builder) =>
      reset(mockedDb, mockedMakeDb) // the mocks are shared between the tests of this spec

      mockedMakeDb(testDbName) returns IO.pure(mockedDb)
      mockedDb.loadRootNodes returns IO.pure((testMetadata, List(boolInNode), List(boolOutNode)))

      async[IO]:
        val graph: MapGraphLake[IO] = builder.load(testDbName, testMapConfig).logValue("load", "graph").await

        mockedMakeDb(testDbName) was called
        mockedDb.loadRootNodes was called

        graph.metadata mustEqual testMetadata
        graph.ioNodes mustEqual Map(boolInNode.name -> boolInNode, boolOutNode.name -> boolOutNode)
