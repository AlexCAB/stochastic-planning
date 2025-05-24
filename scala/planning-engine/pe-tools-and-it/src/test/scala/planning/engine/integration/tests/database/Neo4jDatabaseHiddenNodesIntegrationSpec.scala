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
| created: 2025-05-14 |||||||||||*/

package planning.engine.integration.tests.database

import cats.effect.{IO, Resource}
import cats.effect.cps.*
import neotypes.model.types.Node
import planning.engine.common.properties.PROP_NAME
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.integration.tests.{IntegrationSpecWithResource, WithItDb}
import planning.engine.map.database.Neo4jDatabase
import planning.engine.map.database.Neo4jQueries.*
import planning.engine.map.graph.KnowledgeGraphTestData
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}

class Neo4jDatabaseHiddenNodesIntegrationSpec extends IntegrationSpecWithResource[(WithItDb.ItDb, Neo4jDatabase[IO])]
    with WithItDb with KnowledgeGraphTestData:

  override val resource: Resource[IO, (WithItDb.ItDb, Neo4jDatabase[IO])] =
    for
      itDb <- makeDb
      neo4jdb <- Neo4jDatabase[IO](itDb.config, itDb.dbName)
      _ <- Resource.eval(neo4jdb.initDatabase(graphDbData).logValue)
    yield (itDb, neo4jdb)

  "Neo4jDatabase.createConcreteNode(...)" should:
    "create concrete node in DB" in: (itDb, neo4jdb) =>
      import neotypes.syntax.all.*
      given WithItDb.ItDb = itDb

      val id: HnId = HnId(1234)
      val name: Option[Name] = Some(Name("testConcreteName_1"))
      val ioValueIndex: IoIndex = IoIndex(4321)

      async[IO]:
        val neo4jNodes: List[Node] = neo4jdb
          .createConcreteNode(boolInNode.name, ConcreteNode.makeDbParams[IO](id, name, ioValueIndex).await)
          .await

        val (ioNode, gotConcreteNode) = (neo4jNodes.head, neo4jNodes(1))

        ioNode.getStringProperty(PROP_NAME.NAME) mustEqual boolInNode.name.value
        gotConcreteNode.labels must contain(CONCRETE_LABEL.toLowerCase())
        gotConcreteNode.getLongProperty(PROP_NAME.HN_ID) mustEqual id.value
        gotConcreteNode.getStringProperty(PROP_NAME.NAME) mustEqual name.get.value
        gotConcreteNode.getLongProperty(PROP_NAME.IO_INDEX) mustEqual ioValueIndex.value

        val dbConcreteNode =
          c"""
            MATCH (cn:#${CONCRETE_LABEL} {#${PROP_NAME.NAME}: ${name.get.value}})
                  -->(:#${IO_NODE_LABEL} {#${PROP_NAME.NAME}: ${boolInNode.name.value}})
            RETURN cn
            """.singleNode.logValue.await

        dbConcreteNode.getLongProperty(PROP_NAME.HN_ID) mustEqual id.value
        dbConcreteNode.getLongProperty(PROP_NAME.IO_INDEX) mustEqual ioValueIndex.value

  "Neo4jDatabase.createAbstractNode(...)" should:
    "create abstract node in DB" in: (itDb, neo4jdb) =>
      import neotypes.syntax.all.*
      given WithItDb.ItDb = itDb

      val id: HnId = HnId(1234)
      val name: Option[Name] = Some(Name("testAbstractName_1"))

      async[IO]:
        val gotAbstractNode: Node = neo4jdb
          .createAbstractNode(AbstractNode.makeDbParams[IO](id, name).await)
          .await

        gotAbstractNode.labels must contain(ABSTRACT_LABEL.toLowerCase())
        gotAbstractNode.getLongProperty(PROP_NAME.HN_ID) mustEqual id.value
        gotAbstractNode.getStringProperty(PROP_NAME.NAME) mustEqual name.get.value

        val dbAbstractNode =
          c"""
            MATCH (an:#${ABSTRACT_LABEL} {#${PROP_NAME.NAME}: ${name.get.value}})
            RETURN an
            """.singleNode.logValue.await

        dbAbstractNode.getLongProperty(PROP_NAME.HN_ID) mustEqual id.value
