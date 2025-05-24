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
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import neotypes.cats.effect.implicits.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.common.errors.{assertDistinct, assertionError}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.graph.{MapCacheLike, MapCacheState, MapMetadata}
import planning.engine.map.hidden.node.ConcreteNode

/** Neo4jDatabase is a class that provides a high-level API to interact with a Neo4j database. It is responsible for
  * reading and writing data to the database.
  *
  * This class is a wrapper for the Neo4j database. It is used to connect to the database, clear it, insert root node,
  * etc. Knowledge graph have two parts:
  *   - root graph - have ROOT node, which contains the metadata of the graph and the input and output nodes
  *     definitions. Set of SAMPLE connected to the ROOT node, which contains the sample data.
  *   - hidden graph - contains the hidden nodes and edges, which are used to represent the knowledge map.
  */

trait Neo4jDatabaseLike[F[_]]:
  def initDatabase(metadata: MapMetadata, inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[List[Node]]
  def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]], MapCacheState[F])]
  def createConcreteNodes[R <: MapCacheLike[F]](nodes: List[ConcreteNode[F]], block: => F[R]): F[(List[Node], R)]

//  def createConcreteNodes(params: List[(Name, Map[String, Param])], block: => F[Unit]): F[List[Node]]
//  def createAbstractNodes(params: List[Map[String, Param]]): F[List[Node]]
//  def findHiddenNodesByNames[R](names: List[Name], loadCached: LoadCached[R, F]): F[R]
//  def countHiddenNodes: F[Long]

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], dbName: String) extends Neo4jDatabaseLike[F] with Neo4jQueries:
  private val writeConf: TransactionConfig = TransactionConfig.default.withDatabase(dbName)
  private val readConf: TransactionConfig = TransactionConfig.readOnly.withDatabase(dbName)

  private def splitIoNodes(ioNodes: List[IoNode[F]]): F[(List[InputNode[F]], List[OutputNode[F]])] =
    ioNodes.foldRight((List[InputNode[F]](), List[OutputNode[F]]()).pure):
      case (inNode: InputNode[F], buf)   => buf.map((inNodes, outNodes) => (inNodes :+ inNode, outNodes))
      case (outNode: OutputNode[F], buf) => buf.map((inNodes, outNodes) => (inNodes, outNodes :+ outNode))
      case (node, _)                     => s"Invalid IoNode type: $node".assertionError

  override def initDatabase(
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[List[Node]] = driver.transact(writeConf): tx =>
    for
      mtParams <- metadata.toQueryParams
      inParams <- inNodes.map(_.toQueryParams).sequence
      outParams <- outNodes.map(_.toQueryParams).sequence
      _ <- removeAllNodesQuery(tx)
      staticNodes <- createStaticNodesQuery(mtParams)(tx)
      ioNodes <- (inParams ++ outParams).map((label, params) => createIoNodeQuery(label, params)(tx)).sequence
    yield staticNodes ++ ioNodes

  override def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]], MapCacheState[F])] =
    driver.transact(readConf): tx =>
      for
        List(rootNode, _) <- readStaticNodesQuery(tx)
        metadata <- MapMetadata.fromNode(rootNode)
        nextHnIdId <- findNextHnIdIdQuery(tx).map(HnId.apply)
        nextSampleId <- findNextSampleIdQuery(tx).map(SampleId.apply)
        sampleCount <- countSamplesQuery(tx)
        graphState <- MapCacheState.empty(nextHnIdId, nextSampleId, sampleCount)
        rawIoNodes <- readIoNodesQuery(tx)
        ioNodes <- rawIoNodes.map(n => IoNode.fromNode(n)).sequence
        (inNodes, outNodes) <- splitIoNodes(ioNodes)
      yield (metadata, inNodes, outNodes, graphState)

  override def createConcreteNodes[R <: MapCacheLike[F]](
      nodes: List[ConcreteNode[F]],
      block: => F[R]
  ): F[(List[Node], R)] = driver.transact(writeConf): tx =>
    for
      
      nodes <- params.map((ioNodeName, props) => addConcreteNodeQuery(ioNodeName.value, props)(tx)).sequence
      _ <- block
    yield nodes.flatten

//
//  override def createAbstractNodes(params: List[Map[String, Param]]): F[List[Node]] =
//    driver.transact(writeConf)(tx => params.map(props => addAbstractNodeQuery(props)(tx)).sequence)
//
//  override def findHiddenNodesByNames[R](names: List[Name], loadCached: LoadCached[R, F]): F[R] =
//    driver.transact(readConf): tx =>
//      for
//        ids <- findHiddenIdsNodesByNamesQuery(names.map(_.value))(tx)
//        _ <- ids.assertDistinct("Hidden node ids should be distinct")
//        result <- loadCached(
//          ids.map(HnId.apply),
//          notFoundIds =>
//            for
//              abstractNodes <- findAbstractNodesByIdsQuery[F](notFoundIds.map(_.value))(tx)
//                .map(_.map(n => (n, None)))
//              concreteNodes <- findConcreteNodesByIdsQuery[F](notFoundIds.map(_.value))(tx).flatMap(_
//                .map:
//                  case hn :: ion :: Nil => (hn, Some(ion)).pure
//                  case ns               => s"Expected exactly 2 node but got: $ns".assertionError
//                .sequence)
//              allNodes = abstractNodes ++ concreteNodes
//              _ <- allNodes.map(_._1.elementId).assertDistinct("Hidden node should be distinct")
//            yield allNodes
//        )
//      yield result
//
//  override def countHiddenNodes: F[Long] = driver.transact(readConf)(tx => countAllHiddenNodesQuery(tx))

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)
