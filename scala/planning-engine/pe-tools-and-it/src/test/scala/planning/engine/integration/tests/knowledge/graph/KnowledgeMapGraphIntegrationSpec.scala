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
| created: 2025-06-16 |||||||||||*/

package planning.engine.integration.tests.knowledge.graph

import cats.effect.{IO, Resource}
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestMapGraph
import planning.engine.integration.tests.{IntegrationSpecWithResource, MapGraphIntegrationTestData, WithItDb}
import cats.effect.cps.*
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import cats.syntax.all.*
import planning.engine.map.graph.MapCacheState
import neotypes.syntax.all.*
import planning.engine.common.values.db.Neo4j.*

class KnowledgeMapGraphIntegrationSpec extends IntegrationSpecWithResource[TestMapGraph]
    with WithItDb with MapGraphIntegrationTestData:

  override val resource: Resource[IO, TestMapGraph] =
    for
      itDb <- makeDb()
      neo4jdb <- createRootNodesInDb(itDb.config, itDb.dbName)
      concreteNames = makeNames("concrete", 3)
      abstractNames = makeNames("abstract", 3)
      nodes <- Resource.eval(createTestHiddenNodesInDb(neo4jdb, concreteNames, abstractNames))
      graph <- loadTestMapGraph(neo4jdb)
    yield TestMapGraph(itDb, neo4jdb, nodes, graph)

  "MapGraph.getState(...)" should:
    "return MapCacheState" in: res =>
      async[IO]:
        val state = res.graph.getState.logValue("return MapCacheState", "state").await
        state.sampleCount must be >= 0L

  "MapGraph.getIoNode(...)" should:
    "return IO node" in: res =>
      async[IO]:
        val inNode = res.graph.getIoNode(boolInNode.name).logValue("return IO node", "node").await
        val outNode = res.graph.getIoNode(intOutNode.name).logValue("return IO node", "node").await

        inNode mustEqual boolInNode
        outNode mustEqual intOutNode

  "MapGraph.newConcreteNodes(...)" should:
    "create new concrete nodes and cache them" in: res =>
      given WithItDb.ItDb = res.itDb
      async[IO]:
        val newConcreteNodes = List(
          ConcreteNode.New(Some(Name("test-concrete-1")), intInNode.name, IoIndex(101L)),
          ConcreteNode.New(Some(Name("test-concrete-2")), boolOutNode.name, IoIndex(102L)),
          ConcreteNode.New(None, intOutNode.name, IoIndex(103L))
        )

        val createdNodes: List[ConcreteNode[IO]] = res.graph.newConcreteNodes(newConcreteNodes).await
        createdNodes.traverse(n => logInfo("created concrete node", s"node = $n")).await

        createdNodes.size mustEqual 3
        createdNodes.map(_.name) mustEqual newConcreteNodes.map(_.name)
        createdNodes.map(_.ioNode) mustEqual List(intInNode, boolOutNode, intOutNode)
        createdNodes.map(_.valueIndex) mustEqual newConcreteNodes.map(_.valueIndex)

        val state: MapCacheState[IO] = res.graph.getState.logValue("created concrete node", "state").await

        state.hiddenNodes.keySet must contain allElementsOf createdNodes.map(_.id)
        state.hnQueue must contain allElementsOf createdNodes.map(_.id)

        val dbConcreteNodeIds =
          c"""
            MATCH (cn:#$HN_LABEL:#$CONCRETE_LABEL)-->(:#$IO_LABEL)
            RETURN cn
            """.listHnIds.await

        dbConcreteNodeIds must contain allElementsOf createdNodes.map(_.id)

  "MapGraph.newAbstractNodes(...)" should:
    "create new abstract nodes and cache them" in: res =>
      given WithItDb.ItDb = res.itDb
      async[IO]:
        val newAbstractNodes = List(
          AbstractNode.New(Some(Name("test-abstract-1"))),
          AbstractNode.New(Some(Name("test-abstract-2"))),
          AbstractNode.New(None)
        )

        val createdNodes: List[AbstractNode[IO]] = res.graph.newAbstractNodes(newAbstractNodes).await
        createdNodes.traverse(n => logInfo("created abstract node", s"node = $n")).await

        createdNodes.size mustEqual 3
        createdNodes.map(_.name) mustEqual newAbstractNodes.map(_.name)

        val state: MapCacheState[IO] = res.graph.getState.logValue("created abstract node", "state").await

        state.hiddenNodes.keySet must contain allElementsOf createdNodes.map(_.id)
        state.hnQueue must contain allElementsOf createdNodes.map(_.id)

        val dbAbstractNodeIds = c"MATCH (an:#$HN_LABEL:#$ABSTRACT_LABEL) RETURN an".listHnIds.await

        dbAbstractNodeIds must contain allElementsOf createdNodes.map(_.id)

  "MapGraph.findHiddenNodesByNames(...)" should:
    "find nodes by names" in: res =>
      async[IO]:
        val namesToFind = List(res.nodes.abstractNodes.head.name.get, res.nodes.concreteNodes.head.name.get)
        val foundNodes: List[HiddenNode[IO]] = res.graph.findHiddenNodesByNames(namesToFind).await

        foundNodes.traverse(n => logInfo("found hidden node", s"node = $n")).await

        foundNodes.size mustEqual 2
        foundNodes.map(_.name.getOrElse(fail("node name Should not be empty"))).toSet mustEqual namesToFind.toSet

  "MapGraph.countHiddenNodes(...)" should:
    "count hidden nodes" in: res =>
      given WithItDb.ItDb = res.itDb
      async[IO]:
        val gotCount: Long = res.graph.countHiddenNodes.logValue("count hidden nodes", "count").await
        val testCount = c"MATCH (n: #$HN_LABEL) RETURN count(n)".count.await

        gotCount mustEqual testCount
