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
| created: 2025-03-11 |||||||||||*/

package planning.engine.map.database

import cats.effect.Async
import cats.effect.Resource
import neotypes.{AsyncDriver, GraphDatabase, TransactionConfig}
import neotypes.model.types.Node
import planning.engine.common.config.Neo4jConnectionConf
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.knowledge.graph.{KnowledgeGraphState, Metadata}
import planning.engine.map.samples.SamplesState
import neotypes.cats.effect.implicits.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.map.database.model.{HiddenNodeDbData, KnowledgeGraphDbData}
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.common.errors.{assertionError, assertDistinct}

/** Neo4jDatabase is a class that provides a high-level API to interact with a Neo4j database. It is responsible for
  * reading and writing data to the database.
  *
  * This class is a wrapper for the Neo4j database. It is used to connect to the database, clear it, insert root node,
  * etc. Knowledge graph have two parts:
  *   - root graph - have ROOT node, which contains the metadata of the graph and the input and output nodes
  *     definitions. Set of SAMPLE connected to the ROOT node, which contains the sample data.
  *   - hidden graph - contains the hidden nodes and edges, which are used to represent the knowledge map.
  */

type LoadCached[R, F[_]] = (List[HnId], LoadFound[F]) => F[R]
type LoadFound[F[_]] = List[HnId] => F[List[HiddenNodeDbData]]

trait Neo4jDatabaseLike[F[_]]:
  def initDatabase(data: KnowledgeGraphDbData[F]): F[List[Node]]
  def loadRootNodes: F[KnowledgeGraphDbData[F]]
  def createConcreteNode(ioNodeName: Name, params: Map[String, Param]): F[List[Node]]
  def createAbstractNode(params: Map[String, Param]): F[Node]
  def findHiddenNodesByNames[R](names: List[Name], loadCached: LoadCached[R, F]): F[R]
  def countHiddenNodes: F[Long]

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], dbName: String) extends Neo4jDatabaseLike[F] with Neo4jQueries:
  private val writeConf: TransactionConfig = TransactionConfig.default.withDatabase(dbName)
  private val readConf: TransactionConfig = TransactionConfig.readOnly.withDatabase(dbName)

  private def splitIoNodes(ioNodes: List[IoNode[F]]): (List[InputNode[F]], List[OutputNode[F]]) =
    ioNodes.foldRight((List[InputNode[F]](), List[OutputNode[F]]())):
      case (inNode: InputNode[F], (inNodes, outNodes))   => (inNodes :+ inNode, outNodes)
      case (outNode: OutputNode[F], (inNodes, outNodes)) => (inNodes, outNodes :+ outNode)
      case (node, _)                                     => throw AssertionError(s"Invalid IoNode type: $node")

  override def initDatabase(data: KnowledgeGraphDbData[F]): F[List[Node]] = driver.transact(writeConf): tx =>
    for
      mtParams <- data.metadata.toQueryParams
      graphStateParams <- data.graphState.toQueryParams
      inParams <- data.inNodes.map(_.toQueryParams).sequence
      outParams <- data.outNodes.map(_.toQueryParams).sequence
      sampleParams <- data.samplesState.toQueryParams
      _ <- removeAllNodesQuery(tx)
      staticNodes <- createStaticNodesQuery(mtParams ++ graphStateParams, sampleParams)(tx)
      ioNodes <- (inParams ++ outParams).map((label, params) => createIoNodeQuery(label, params)(tx)).sequence
    yield staticNodes ++ ioNodes

  override def loadRootNodes: F[KnowledgeGraphDbData[F]] = driver.transact(readConf): tx =>
    for
      List(rootNode, samplesNode) <- readStaticNodesQuery(tx)
      metadata <- Metadata.fromNode(rootNode)
      graphState <- KnowledgeGraphState.fromNode(rootNode)
      samplesState <- SamplesState.fromNode(samplesNode)
      rawIoNodes <- readIoNodesQuery(tx)
      ioNodes <- rawIoNodes.map(n => IoNode.fromNode(n)).sequence
      (inNodes, outNodes) = splitIoNodes(ioNodes)
    yield KnowledgeGraphDbData[F](metadata, inNodes, outNodes, samplesState, graphState)

  override def createConcreteNode(ioNodeName: Name, params: Map[String, Param]): F[List[Node]] = driver
    .transact(writeConf)(tx => addConcreteNodeQuery(ioNodeName.value, params)(tx))

  override def createAbstractNode(params: Map[String, Param]): F[Node] = driver
    .transact(writeConf)(tx => addAbstractNodeQuery(params)(tx))

  override def findHiddenNodesByName[R](names: List[Name], loadCached: LoadCached[R, F]): F[R] =
    driver.transact(readConf): tx =>
      for
        ids <- findHiddenIdsNodesByNamesQuery(names.map(_.value))(tx)
        _ <- ids.assertDistinct("Hidden node ids should be distinct")
        result <- loadCached(
          ids.map(HnId.apply),
          notFoundIds =>
            for
              abstractNodes <- findAbstractNodesByIdsQuery[F](notFoundIds.map(_.value))(tx)
                .map(_.map(n => HiddenNodeDbData(n, ioNode = None)))
              concreteNodes <- findConcreteNodesByIdsQuery[F](notFoundIds.map(_.value))(tx).flatMap(_
                .map:
                  case hn :: ion :: Nil => HiddenNodeDbData(hn, Some(ion)).pure
                  case ns               => s"Expected exactly 2 node but got: $ns".assertionError
                .sequence)
              allNodes = abstractNodes ++ concreteNodes
              _ <- allNodes.map(_.hiddenNode.elementId).assertDistinct("Hidden node should be distinct")
            yield allNodes
        )
      yield result

  override def countHiddenNodes: F[Long] = driver.transact(readConf)(tx => countAllHiddenNodesQuery(tx))

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConnectionConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)
