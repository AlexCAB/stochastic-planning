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

package planning.engine.integration.tests

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Resource}
import org.scalatest.matchers.must.Matchers
import planning.engine.common.SpecLogging
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestHiddenNodes
import planning.engine.map.database.{Neo4jConf, Neo4jDatabase}
import planning.engine.map.graph.{MapBuilder, MapGraph, MapGraphTestData}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import cats.syntax.all.*
import neotypes.model.types

trait MapGraphIntegrationTestData extends MapGraphTestData:
  self: AsyncIOSpec & Matchers & SpecLogging =>

  def createRootNodesInDb(dbConfig: Neo4jConf, dbName: String): Resource[IO, Neo4jDatabase[IO]] =
    for
      neo4jdb <- Neo4jDatabase[IO](dbConfig, dbName)
      _ <- Resource.eval(neo4jdb.initDatabase(
        testMapConfig,
        testMetadata,
        List(boolInNode, intInNode),
        List(boolOutNode, intOutNode)
      ).logValue("createRootNodesInDb"))
    yield neo4jdb

  def makeNames(prefix: String, n: Int): List[Option[Name]] =
    (1 to n).toList.map(i => Some(Name(s"$prefix-$i"))) :+ None

  def createTestHiddenNodesInDb(
      neo4jdb: Neo4jDatabase[IO],
      concreteNames: List[Option[Name]],
      abstractNames: List[Option[Name]]
  ): IO[TestHiddenNodes] =
    def makeConcreteNodes: IO[(List[types.Node], List[ConcreteNode[IO]])] = neo4jdb
      .createConcreteNodes(
        numOfNodes = concreteNames.size,
        makeNodes = hnIds =>
          hnIds
            .zip(concreteNames)
            .map((id, name) => ConcreteNode[IO](id, name, intInNode, IoIndex(id.value + 100L)))
            .pure[IO],
        initNextHnIndex = testMapConfig.initNextHnIndex
      )

    def makeAbstractNodes: IO[(List[types.Node], List[AbstractNode[IO]])] = neo4jdb
      .createAbstractNodes(
        numOfNodes = abstractNames.size,
        makeNodes = hnIds => hnIds.zip(abstractNames).map((id, name) => AbstractNode[IO](id, name)).pure[IO],
        initNextHnIndex = testMapConfig.initNextHnIndex
      )

    for
      (_, concreteNodes) <- makeConcreteNodes.logValue("createTestHiddenNodesInDb", "concreteNodes")
      (_, abstractNodes) <- makeAbstractNodes.logValue("createTestHiddenNodesInDb", "abstractNodes")
    yield TestHiddenNodes(concreteNodes, abstractNodes, concreteNodes ++ abstractNodes)

  def loadTestMapGraph(neo4jdb: Neo4jDatabase[IO]): Resource[IO, MapGraph[IO]] =
    for
      builder <- MapBuilder[IO](neo4jdb)
      graph <- Resource.eval(builder.load(testMapConfig))
    yield graph.asInstanceOf[MapGraph[IO]]

object MapGraphIntegrationTestData:
  final case class TestHiddenNodes(
      concreteNodes: List[ConcreteNode[IO]],
      abstractNodes: List[AbstractNode[IO]],
      all: List[HiddenNode[IO]]
  )

  final case class TestMapGraph(
      itDb: WithItDb.ItDb,
      neo4jdb: Neo4jDatabase[IO],
      nodes: TestHiddenNodes,
      graph: MapGraph[IO]
  )
