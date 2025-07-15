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

package planning.engine.integration.tests.database

import cats.effect.{IO, Resource}
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.values.db.Neo4j.*
import neotypes.model.types.Node
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}
import planning.engine.map.database.Neo4jDatabase
import planning.engine.map.graph.{MapGraphTestData, MapMetadata}
import planning.engine.map.io.node.IoNode

class Neo4jDatabaseRootNodesIntegrationSpec extends IntegrationSpecWithResource[(WithItDb.ItDb, Neo4jDatabase[IO])]
    with WithItDb with MapGraphTestData:

  override val resource: Resource[IO, (WithItDb.ItDb, Neo4jDatabase[IO])] =
    for
      itDb <- makeDb()
      neo4jdb <- Resource.eval(Neo4jDatabase[IO](itDb.driver, itDb.dbName))
    yield (itDb, neo4jdb)

  "Neo4jDatabase" should:
    "check connection" in: (_, neo4jdb) =>
      async[IO]:
        val numOfNodes: Long = neo4jdb.checkConnection.logValue("check connection").await
        numOfNodes must be >= 0L

    "create root nodes" in: (itDb, neo4jdb) =>
      import neotypes.syntax.all.*
      given WithItDb.ItDb = itDb

      async[IO]:
        val createdNods: List[Node] = neo4jdb
          .initDatabase(testMapConfig, testMetadata, List(boolInNode), List(boolOutNode))
          .await

        createdNods.traverse(n => logInfo("create root nodes", s"node = $n")).await

        val allRootNodeLabels =
          Set(ROOT_LABEL, SAMPLES_LABEL, IO_NODES_LABEL, IN_LABEL, OUT_LABEL, IO_LABEL, IN_LABEL, OUT_LABEL)

        createdNods.size mustEqual 5
        createdNods.toSet.flatMap(_.labels) mustEqual allRootNodeLabels.map(_.toLowerCase)

        MapMetadata
          .fromNode[IO](c"MATCH (root: #$ROOT_LABEL) RETURN root".singleNode.await)
          .logValue("create").await mustEqual testMetadata

        val ioNodes: List[Node] =
          c"""
          MATCH (:#$ROOT_LABEL)-->(:#$IO_NODES_LABEL)-->(io_nodes:#$IO_LABEL)
          RETURN io_nodes
          """.listNode.await

        ioNodes.size mustEqual 2
        ioNodes.toSet.flatMap(_.labels) mustEqual Set(IO_LABEL, IN_LABEL, OUT_LABEL).map(_.toLowerCase)
        ioNodes.map(n => IoNode.fromNode[IO](n)).sequence.await.toSet mustEqual allIoNodes

    "load them back" in: (_, neo4jdb) =>
      async[IO]:
        val loadData = neo4jdb.loadRootNodes.logValue("load").await

        loadData._1 mustEqual testMetadata
        loadData._2 mustEqual List(boolInNode)
        loadData._3 mustEqual List(boolOutNode)
