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
import planning.engine.common.properties.*
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.integration.tests.{IntegrationSpecWithResource, MapGraphIntegrationTestData, WithItDb}
import planning.engine.map.database.Neo4jDatabase
import planning.engine.common.values.db.Neo4j.*
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import cats.effect.cps.*
import cats.syntax.all.*
import neotypes.model.types.Node
import neotypes.syntax.all.*
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestHiddenNodes

class Neo4jDatabaseHiddenNodesIntegrationSpec extends IntegrationSpecWithResource[(WithItDb.ItDb, Neo4jDatabase[IO])]
    with WithItDb with MapGraphIntegrationTestData:

  override val removeDbAfterTest: Boolean = true

  override val resource: Resource[IO, (WithItDb.ItDb, Neo4jDatabase[IO])] =
    for
      itDb <- makeDb()
      neo4jdb <- createRootNodesInDb(itDb.config, itDb.dbName)
    yield (itDb, neo4jdb)

  private def getNextHnId(implicit db: WithItDb.ItDb): IO[Long] =
    c"MATCH (r:#$ROOT_LABEL) RETURN r".singleNode.map(_.getLongProperty(PROP.NEXT_HN_ID))

  "Neo4jDatabase.createConcreteNodes(...)" should:
    "create concrete nodes in DB" in: (itDb, neo4jdb) =>
      given WithItDb.ItDb = itDb

      def getHiddenNodes(id: HnId)(implicit db: WithItDb.ItDb): IO[List[Node]] =
        c"MATCH (cn: #$HN_LABEL: #$CONCRETE_LABEL  {#${PROP.HN_ID}: ${id.value}})-->(io: #$IO_LABEL)RETURN [io, cn]"
          .listListNode.map(_.flatten)

      async[IO]:
        val nextHnId = getNextHnId.await

        val newNodes = List(
          ConcreteNode.New(Some(Name("inputNode")), boolInNode.name, IoIndex(10L)),
          ConcreteNode.New(Some(Name("outputNode")), boolOutNode.name, IoIndex(20L))
        )

        val concreteNodeIds: List[HnId] = neo4jdb.createConcreteNodes(testMapConfig.initNextHnIndex, newNodes).await
        val rawNodes: List[Node] = concreteNodeIds.flatMap(id => getHiddenNodes(id).await)

        val expectedLabels = Set(IO_LABEL, IN_LABEL, OUT_LABEL, HN_LABEL, CONCRETE_LABEL).map(_.toLowerCase)
        val expectedHnIds = (nextHnId until nextHnId + 2L).toList.map(HnId.apply)
        val expecteIoNodeNames = Set(boolInNode.name.value, boolOutNode.name.value)

        concreteNodeIds.size mustEqual 2
        concreteNodeIds mustEqual expectedHnIds

        rawNodes.size mustEqual 4
        rawNodes.toSet.flatMap(_.labels) mustEqual expectedLabels

        val conRawNode = rawNodes.filter(_.is(CONCRETE_LABEL))
        val ioRawNode = rawNodes.filter(_.is(IO_LABEL))

        conRawNode.size mustEqual 2
        ioRawNode.size mustEqual 2

        conRawNode.map(_.getLongProperty(PROP.HN_ID)).toSet mustEqual expectedHnIds.map(_.value).toSet
        conRawNode.map(_.getStringProperty(PROP.NAME)).toSet mustEqual newNodes.map(_.name.get.value).toSet
        conRawNode.map(_.getLongProperty(PROP.IO_INDEX)).toSet mustEqual newNodes.map(_.valueIndex.value).toSet
        conRawNode.map(_.getLongProperty(PROP.NEXT_HN_INDEX)).toSet mustEqual Set(testMapConfig.initNextHnIndex)
        ioRawNode.map(_.getStringProperty(PROP.NAME)).toSet mustEqual expecteIoNodeNames

        getNextHnId.await mustEqual nextHnId + 2L

  "Neo4jDatabase.createAbstractNode(...)" should:
    "create abstract node in DB" in: (itDb, neo4jdb) =>
      given WithItDb.ItDb = itDb

      def getHiddenNodes(id: HnId)(implicit db: WithItDb.ItDb): IO[Node] =
        c"MATCH (node: #$HN_LABEL: #$ABSTRACT_LABEL  {#${PROP.HN_ID}: ${id.value}}) RETURN node".singleNode

      async[IO]:
        val nextHnId = getNextHnId.await

        val newNodes = List(
          AbstractNode.New(Some(Name("AbstractNode1"))),
          AbstractNode.New(Some(Name("AbstractNode2")))
        )

        val abstractNodeIds: List[HnId] = neo4jdb.createAbstractNodes(testMapConfig.initNextHnIndex, newNodes).await
        val rawNodes: List[Node] = abstractNodeIds.traverse(id => getHiddenNodes(id)).await

        val expectedHnIds = (nextHnId until nextHnId + 2L).toList.map(HnId.apply)

        rawNodes.size mustEqual 2
        rawNodes.toSet.flatMap(_.labels) mustEqual Set(HN_LABEL, ABSTRACT_LABEL).map(_.toLowerCase)

        abstractNodeIds.size mustEqual 2
        abstractNodeIds mustEqual expectedHnIds
        rawNodes.map(_.getLongProperty(PROP.HN_ID)).toSet mustEqual expectedHnIds.map(_.value).toSet

        getNextHnId.await mustEqual nextHnId + 2L

  "Neo4jDatabase.findHiddenNodesByNames(...)" should:
    "find hidden nodes" in: (_, neo4jdb) =>
      async[IO]:
        val name1 = Name("find_node_con_1")
        val name2 = Name("find_node_abs_1")
        val concreteNames = List(Some(name1), Some(name1), Some(Name("find_node_con_2")), None)
        val abstractNames = List(Some(name2), Some(Name("find_node_abs_2")), None)
        val nodes: TestHiddenNodes = createTestHiddenNodesInDb(neo4jdb, concreteNames, abstractNames).await
        val expectedHnIds = Map(name1 -> nodes.findHnIdsForName(name1), name2 -> nodes.findHnIdsForName(name2))

        logInfo("find hidden nodes", s" created nodes = $nodes").await

        val foundNodes: Map[Name, Set[HiddenNode[IO]]] = neo4jdb
          .findHiddenNodesByNames(
            names = Set(name1, name2),
            getIoNode = name =>
              for
                _ <- logInfo("find hidden nodes", s"getIoNode: getIoNode.name = $name")
                _ <- IO.delay(name mustEqual intInNode.name)
              yield intInNode
          ).await

        foundNodes.toList
          .traverse((name, nodes) => logInfo("find hidden nodes", s"found name = $name, nodes = $nodes"))
          .await

        foundNodes.size mustEqual 2
        foundNodes.keys.toSet mustEqual Set(name1, name2)
        foundNodes(name1).size mustEqual 2
        foundNodes(name1).map(_.name).toSet mustEqual Set(Some(name1))
        foundNodes(name2).size mustEqual 1
        foundNodes(name2).map(_.name).toSet mustEqual Set(Some(name2))
        foundNodes.map((name, nodes) => name -> nodes.map(_.id)) mustEqual expectedHnIds

  "Neo4jDatabase.findHnIdsByNames(...)" should:
    "find hidden nodes IDs" in: (_, neo4jdb) =>
      async[IO]:
        val name1 = Name("find_id_con_1")
        val name2 = Name("find_id_abs_1")
        val concreteNames = List(Some(name1), Some(name1), Some(Name("find_id_con_2")), None)
        val abstractNames = List(Some(name2), Some(Name("find_id_abs_2")), None)
        val nodes: TestHiddenNodes = createTestHiddenNodesInDb(neo4jdb, concreteNames, abstractNames).await
        val expectedHnIds = Map(name1 -> nodes.findHnIdsForName(name1), name2 -> nodes.findHnIdsForName(name2))

        logInfo("find hidden nodes IDs", s" created nodes = $nodes").await
        val foundIds: Map[Name, Set[HnId]] = neo4jdb.findHnIdsByNames(Set(name1, name2)).await
        logInfo("find hidden nodes", s"foundIds = $foundIds").await

        foundIds.size mustEqual 2
        foundIds.keys.toSet mustEqual Set(name1, name2)
        foundIds(name1).size mustEqual 2
        foundIds(name2).size mustEqual 1
        foundIds mustEqual expectedHnIds

  "Neo4jDatabase.countHiddenNodes(...)" should:
    "count hidden nodes" in: (itDb, neo4jdb) =>
      given WithItDb.ItDb = itDb
      async[IO]:
        val nextHnId = getNextHnId.await
        val numOfNodes = neo4jdb.countHiddenNodes.logValue("count hidden nodes").await
        numOfNodes mustEqual (nextHnId - 1L)
