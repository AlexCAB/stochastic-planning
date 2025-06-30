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
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import cats.effect.cps.*
import cats.syntax.all.*
import neotypes.syntax.all.*

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
      async[IO]:
        val nextHnId = getNextHnId.await
        val (rawNodes, concreteNodes) = neo4jdb
          .createConcreteNodes(
            numOfNodes = 3L,
            makeNodes = hnIds =>
              for
                _ <- logInfo("create concrete nodes in DB", s"hnIds = $hnIds")
                _ <- IO.delay(hnIds.size mustEqual 3)
                _ <- IO.delay(hnIds.toSet.size mustEqual 3)
                nodes = hnIds.map(id =>
                  ConcreteNode[IO](id, Some(Name(s"con_${id.value}")), intInNode, IoIndex(id.value + 100L))
                )
              yield nodes,
            initNextHnIndex = testMapConfig.initNextHnIndex
          ).await

        val expectedLabels = Set(IO_LABEL, IN_LABEL, HN_LABEL, CONCRETE_LABEL).map(_.toLowerCase)
        val expectedHnIds = (nextHnId until nextHnId + 3L).toList.map(HnId.apply)
        val expectedNames = (nextHnId until nextHnId + 3L).toList.map(i => Some(Name(s"con_$i")))

        rawNodes.size mustEqual 6
        rawNodes.toSet.flatMap(_.labels) mustEqual expectedLabels
        concreteNodes.size mustEqual 3
        concreteNodes.map(_.id) mustEqual expectedHnIds
        concreteNodes.map(_.name) mustEqual expectedNames
        concreteNodes.map(_.ioNode).map(_.name).toSet mustEqual Set(Name("intInputNode"))
        concreteNodes.map(_.valueIndex) mustEqual List(IoIndex(101), IoIndex(102), IoIndex(103))

        val concreteNode = concreteNodes.head

        val dbConcreteNode =
          c"""
            MATCH (cn:#$HN_LABEL:#$CONCRETE_LABEL {#${PROP.NAME}: ${concreteNode.name.get.value}})
                  -->(:#$IO_LABEL {#${PROP.NAME}: ${intInNode.name.value}})
            RETURN cn
            """.singleNode.await

        dbConcreteNode.getLongProperty(PROP.HN_ID) mustEqual concreteNode.id.value
        dbConcreteNode.getLongProperty(PROP.IO_INDEX) mustEqual concreteNode.valueIndex.value
        getNextHnId.await mustEqual nextHnId + 3L

  "Neo4jDatabase.createAbstractNode(...)" should:
    "create abstract node in DB" in: (itDb, neo4jdb) =>
      given WithItDb.ItDb = itDb
      async[IO]:
        val nextHnId = getNextHnId.await
        val (rawNodes, abstractNodes) = neo4jdb
          .createAbstractNodes(
            numOfNodes = 3L,
            makeNodes = hnIds =>
              for
                _ <- logInfo("create abstract nodes in DB", s"hnIds = $hnIds")
                _ <- IO.delay(hnIds.size mustEqual 3)
                _ <- IO.delay(hnIds.toSet.size mustEqual 3)
                nodes = hnIds.map(id => AbstractNode[IO](id, Some(Name(s"abs_${id.value}"))))
              yield nodes,
            initNextHnIndex = testMapConfig.initNextHnIndex
          ).await

        val expectedHnIds = (nextHnId until nextHnId + 3L).toList.map(HnId.apply)
        val expectedNames = (nextHnId until nextHnId + 3L).toList.map(i => Some(Name(s"abs_$i")))

        rawNodes.size mustEqual 3
        rawNodes.toSet.flatMap(_.labels) mustEqual Set(HN_LABEL, ABSTRACT_LABEL).map(_.toLowerCase)
        abstractNodes.size mustEqual 3
        abstractNodes.map(_.id) mustEqual expectedHnIds
        abstractNodes.map(_.name) mustEqual expectedNames
        getNextHnId.await mustEqual nextHnId + 3L

  "Neo4jDatabase.findHiddenNodesByNames(...)" should:
    "find hidden nodes" in: (itDb, neo4jdb) =>
      async[IO]:
        val concreteNames = List(Some(Name("find_con_1")), Some(Name("find_con_1")), Some(Name("find_con_2")), None)
        val abstractNames = List(Some(Name("find_abs_1")), Some(Name("find_abs_2")), None)
        val nodes = createTestHiddenNodesInDb(neo4jdb, concreteNames, abstractNames).await

        logInfo("find hidden nodes", s" created nodes = $nodes").await

        val foundNodes = neo4jdb
          .findHiddenNodesByNames(
            names = List(concreteNames.head.get, abstractNames.head.get),
            getIoNode = name =>
              for
                _ <- logInfo("find hidden nodes", s"getIoNode: getIoNode.name = $name")
                _ <- IO.delay(name mustEqual intInNode.name)
              yield intInNode
          ).await

        foundNodes.traverse(n => logInfo("find hidden nodes", s" found node = $n")).await

        foundNodes.size mustEqual 3
        foundNodes.map(_.name).toSet mustEqual Set(concreteNames.head, abstractNames.head)

  "Neo4jDatabase.countHiddenNodes(...)" should:
    "count hidden nodes" in: (itDb, neo4jdb) =>
      given WithItDb.ItDb = itDb
      async[IO]:
        val nextHnId = getNextHnId.await
        val numOfNodes = neo4jdb.countHiddenNodes.logValue("count hidden nodes").await
        numOfNodes mustEqual (nextHnId - 1L)
