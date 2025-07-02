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
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestHiddenNodes
import planning.engine.map.database.{Neo4jConf, Neo4jDatabase}
import planning.engine.map.graph.{MapBuilder, MapGraph, MapGraphTestData}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import cats.syntax.all.*

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
    def makeConcreteNodes(newNode: ConcreteNode.New): IO[(HnId, ConcreteNode.New)] = neo4jdb
      .createConcreteNodes(testMapConfig.initNextHnIndex, List(newNode))
      .map(ln => ln.headOption.getOrElse(fail(s"No concrete node created, for newNode: $newNode")) -> newNode)

    def makeAbstractNodes(newNode: AbstractNode.New): IO[(HnId, AbstractNode.New)] = neo4jdb
      .createAbstractNodes(testMapConfig.initNextHnIndex, List(newNode))
      .map(ln => ln.headOption.getOrElse(fail(s"No abstract node created, for name: $newNode")) -> newNode)

    for
      concreteNodes <- concreteNames
        .traverse(name => makeConcreteNodes(ConcreteNode.New(name, intInNode.name, IoIndex(123L))))
        .map(_.toMap)
        .logValue("createTestHiddenNodesInDb", "concreteNodes")
      abstractNodes <- abstractNames
        .traverse(name => makeAbstractNodes(AbstractNode.New(name)))
        .map(_.toMap)
        .logValue("createTestHiddenNodesInDb", "abstractNodes")
    yield TestHiddenNodes(concreteNodes, abstractNodes, (concreteNodes.keys ++ abstractNodes.keys).toList)

  def loadTestMapGraph(neo4jdb: Neo4jDatabase[IO]): Resource[IO, MapGraph[IO]] =
    for
      builder <- MapBuilder[IO](neo4jdb)
      graph <- Resource.eval(builder.load(testMapConfig))
    yield graph.asInstanceOf[MapGraph[IO]]

object MapGraphIntegrationTestData:
  final case class TestHiddenNodes(
      concreteNodes: Map[HnId, ConcreteNode.New],
      abstractNodes: Map[HnId, AbstractNode.New],
      allNodeIds: List[HnId]
  )

  final case class TestMapGraph(
      itDb: WithItDb.ItDb,
      neo4jdb: Neo4jDatabase[IO],
      nodes: TestHiddenNodes,
      graph: MapGraph[IO]
  )
