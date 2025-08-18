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
import planning.engine.integration.tests.MapGraphIntegrationTestData.*
import planning.engine.map.{MapBuilder, MapGraph, MapGraphTestData}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import cats.syntax.all.*
import neotypes.GraphDatabase
import planning.engine.common.values.sample.SampleId
import planning.engine.map.io.node.IoNode
import planning.engine.map.samples.sample.Sample
import planning.engine.common.values.db.DbName
import neotypes.cats.effect.implicits.*
import planning.engine.database.{Neo4jConf, Neo4jDatabase}

trait MapGraphIntegrationTestData extends MapGraphTestData:
  self: AsyncIOSpec & Matchers & SpecLogging =>

  def createRootNodesInDb(dbConfig: Neo4jConf, dbName: DbName): Resource[IO, Neo4jDatabase[IO]] =
    for
      driver <- GraphDatabase.asyncDriver[IO](dbConfig.uri, dbConfig.authToken)
      neo4jdb <- Resource.eval(Neo4jDatabase[IO](driver, dbName))
      _ <- Resource.eval(neo4jdb.initDatabase(
        testMapConfig,
        testMetadata,
        List(boolInNode, intInNode),
        List(boolOutNode, intOutNode)
      ).logValue("createRootNodesInDb"))
    yield neo4jdb

  def makeAbsNames(prefix: String, n: Int): List[Option[Name]] =
    (1 to n).toList.map(i => Name.some(s"$prefix-$i")) :+ None
    
  def makeConNames(prefix: String, n: Int): List[(Option[Name], Name, IoIndex)] =
    makeAbsNames(prefix, n).map(n => (n, intInNode.name, IoIndex(123L)))

  def createTestHiddenNodesInDb(
      neo4jdb: Neo4jDatabase[IO],
      concreteNames: List[(Option[Name], Name, IoIndex)],
      abstractNames: List[Option[Name]]
  ): IO[TestHiddenNodes] =
    def makeConcreteNodes(newNode: ConcreteNode.New): IO[(HnId, ConcreteNode.New)] = neo4jdb
      .createConcreteNodes(testMapConfig.initNextHnIndex, List(newNode))
      .map(ln => ln.headOption.getOrElse(fail(s"No concrete node created, for newNode: $newNode"))._1 -> newNode)

    def makeAbstractNodes(newNode: AbstractNode.New): IO[(HnId, AbstractNode.New)] = neo4jdb
      .createAbstractNodes(testMapConfig.initNextHnIndex, List(newNode))
      .map(ln => ln.headOption.getOrElse(fail(s"No abstract node created, for name: $newNode"))._1 -> newNode)

    for
      concreteNodes <- concreteNames
        .traverse((name, ioName, ioIndex) => makeConcreteNodes(ConcreteNode.New(name, None, ioName, ioIndex)))
        .map(_.toMap)
        .logValue("createTestHiddenNodesInDb", "concreteNodes")
      abstractNodes <- abstractNames
        .traverse(name => makeAbstractNodes(AbstractNode.New(name, None)))
        .map(_.toMap)
        .logValue("createTestHiddenNodesInDb", "abstractNodes")
    yield TestHiddenNodes(
      concreteNodes = concreteNodes,
      abstractNodes = abstractNodes,
      allNodeIds = (concreteNodes.keys ++ abstractNodes.keys).toList,
      ioNodeMap = Map(intInNode.name -> intInNode)
    )

  def loadTestMapGraph(neo4jdb: Neo4jDatabase[IO]): Resource[IO, MapGraph[IO]] =
    for
      builder <- Resource.pure(new MapBuilder[IO](_ => neo4jdb.pure[IO]))
      graph <- Resource.eval(builder.load(testDbName, testMapConfig))
    yield graph.asInstanceOf[MapGraph[IO]]

  def initHiddenNodesInDb(
      neo4jdb: Neo4jDatabase[IO],
      concreteNames: List[(Option[Name], Name, IoIndex)],
      abstractNames: List[Option[Name]]
  ): Resource[IO, TestHiddenNodes] = Resource.eval(
    createTestHiddenNodesInDb(neo4jdb, concreteNames, abstractNames)
  )

  def initSampleInDb(neo4jdb: Neo4jDatabase[IO], params: Sample.ListNew): Resource[IO, TestSamples] = Resource.eval:
    for
      (sampleIds, _) <- neo4jdb.createSamples(params).logValue("initSampleInDb", "sampleIds")
      samples <- neo4jdb.getSamples(sampleIds)

      sortedSamples = samples.values.toList.sortBy(_.data.id.value)
      _ <- logInfo("initSampleInDb", s"Created samples:\n${sortedSamples.mkString("\n")}")
      _ = samples.keySet mustEqual sampleIds.toSet
      _ = samples.map(_._2.data.id).toSet mustEqual sampleIds.toSet
    yield TestSamples(
      allHnIds = params.list.flatMap(_.edges).toSet.flatMap(e => Set(e.source, e.target)),
      allSampleIds = sampleIds.toSet,
      samples = samples
    )

object MapGraphIntegrationTestData extends Matchers:
  final case class TestHiddenNodes(
      concreteNodes: Map[HnId, ConcreteNode.New],
      abstractNodes: Map[HnId, AbstractNode.New],
      allNodeIds: List[HnId],
      ioNodeMap: Map[Name, IoNode[IO]]
  ):
    def findHnIdsForName(name: Name): Set[HnId] = concreteNodes
      .map((i, n) => (i, n.name))
      .++(abstractNodes.map((i, n) => (i, n.name)))
      .filter((_, n) => n.contains(name))
      .keys.toSet

    def getIoNode(name: Name): IO[IoNode[IO]] = ioNodeMap
      .getOrElse(name, fail(s"IO Node with name $name not found in $ioNodeMap"))
      .pure[IO]

  final case class TestSamples(
      allHnIds: Set[HnId],
      allSampleIds: Set[SampleId],
      samples: Map[SampleId, Sample]
  )

  object TestSamples:
    def empty: TestSamples = TestSamples(Set.empty, Set.empty, Map.empty)

  final case class TestMapGraph(
      itDb: WithItDb.ItDb,
      neo4jdb: Neo4jDatabase[IO],
      nodes: TestHiddenNodes,
      samples: TestSamples,
      graph: MapGraph[IO]
  )
