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
import neotypes.model.types.Value
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}
import planning.engine.map.database.Neo4jDatabase
import planning.engine.map.knowledge.graph.{KnowledgeGraphTestData, Metadata}
import planning.engine.map.samples.SamplesState
import planning.engine.map.database.Neo4jQueries.*
import planning.engine.map.io.node.IoNode
import planning.engine.map.io.node.IoNode.IO_TYPE_PROP_NAME

type TestResource = (WithItDb.ItDb, Neo4jDatabase[IO])

class Neo4jDatabaseRootNodesIntegrationSpec extends IntegrationSpecWithResource[TestResource] with WithItDb
    with KnowledgeGraphTestData:

  override val resource: Resource[IO, TestResource] =
    for
      itDb <- makeDb
      neo4jdb <- Neo4jDatabase[IO](itDb.config, itDb.dbName)
    yield (itDb, neo4jdb)

  "Neo4jDatabase" should:
    "create root nodes and load them back" in: (itDb, neo4jdb) =>
      import neotypes.syntax.all.*
      given WithItDb.ItDb = itDb

      async[IO]:
        val createdNods = neo4jdb
          .initDatabase(testMetadata, Vector(boolInNode), Vector(boolOutNode), emptySamplesState)
          .logValue.await

        createdNods.size mustEqual 5
        createdNods.toSet.flatMap(_.labels).map(_.toUpperCase) mustEqual allRootNodeLabels

        Metadata
          .fromNode[IO](c"MATCH (root:#${ROOT_LABEL}) RETURN root".singleNode.await)
          .await mustEqual testMetadata

        SamplesState
          .fromNode[IO](c"MATCH (:#${ROOT_LABEL})-->(samples: SAMPLES) RETURN samples".singleNode.await)
          .await mustEqual emptySamplesState

        val ioNodes =
          c"""
               MATCH (:#${ROOT_LABEL})-->(:#${IO_NODES_LABEL})-->(io_nodes:#${IO_NODE_LABEL})
               RETURN io_nodes
               """.listNode.await

        ioNodes.size mustEqual 2

        ioNodes
          .map(n =>
            n
              .properties
              .getOrElse(IO_TYPE_PROP_NAME, fail(s"Not found property ${IO_TYPE_PROP_NAME} in $n"))
          )
          .map:
            case Value.Str(v) => v
            case v            => fail(s"Invalid IoNode type value: $v, expected string")
          .toSet mustEqual allIoNodeTypes

        ioNodes.map(n => IoNode.fromProperties[IO](n.properties)).sequence.await.toSet mustEqual allIoNodes

        val (gotMetadata, gotInNodes, gotOutNodes, gotSamplesState) = neo4jdb.loadRootNodes.logValue.await

        gotMetadata mustEqual testMetadata
        gotInNodes mustEqual Vector(boolInNode)
        gotOutNodes mustEqual Vector(boolOutNode)
        gotSamplesState mustEqual emptySamplesState
