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
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.common.errors.{assertDistinct, assertionError}
import planning.engine.map.graph.{MapCacheLike, MapCacheState, MapMetadata}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}

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

  def createConcreteNodes[R <: MapCacheLike[F]](
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[ConcreteNode[F]]],
      updateCache: List[ConcreteNode[F]] => F[R]
  ): F[(R, List[Node], List[ConcreteNode[F]])]

  def createAbstractNodes[R <: MapCacheLike[F]](
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[AbstractNode[F]]],
      updateCache: List[AbstractNode[F]] => F[R]
  ): F[(R, List[Node], List[AbstractNode[F]])]

  def findHiddenNodesByNames[R <: MapCacheLike[F]](
      names: List[Name],
      loadCached: List[HnId] => F[(R, List[HiddenNode[F]])],
      getIoNode: Name => F[IoNode[F]],
      updateCache: (R, List[HiddenNode[F]]) => F[R]
  ): F[(R, List[HiddenNode[F]])]

  def countHiddenNodes: F[Long]

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], dbName: String) extends Neo4jDatabaseLike[F] with Neo4jQueries:
  private val writeConf: TransactionConfig = TransactionConfig.default.withDatabase(dbName)
  private val readConf: TransactionConfig = TransactionConfig.readOnly.withDatabase(dbName)

  override def initDatabase(
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[List[Node]] = driver.transact(writeConf): tx =>
    for
      mtParams <- metadata.toQueryParams
      inParams <- inNodes.traverse(_.toQueryParams)
      outParams <- outNodes.traverse(_.toQueryParams)
      _ <- removeAllNodesQuery(tx)
      staticNodes <- createStaticNodesQuery(mtParams)(tx)
      ioNodes <- (inParams ++ outParams).traverse((label, params) => createIoNodeQuery(label, params)(tx))
    yield staticNodes ++ ioNodes

  override def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]], MapCacheState[F])] =
    def splitIoNodes(ioNodes: List[IoNode[F]]): F[(List[InputNode[F]], List[OutputNode[F]])] =
      ioNodes.foldRight((List[InputNode[F]](), List[OutputNode[F]]()).pure):
        case (inNode: InputNode[F], buf)   => buf.map((inNodes, outNodes) => (inNodes :+ inNode, outNodes))
        case (outNode: OutputNode[F], buf) => buf.map((inNodes, outNodes) => (inNodes, outNodes :+ outNode))
        case (node, _)                     => s"Invalid IoNode type: $node".assertionError

    driver.transact(readConf): tx =>
      for
        List(rootNode, _) <- readStaticNodesQuery(tx)
        metadata <- MapMetadata.fromNode(rootNode)
        sampleCount <- countSamplesQuery(tx)
        graphState <- MapCacheState.init(sampleCount)
        rawIoNodes <- readIoNodesQuery(tx)
        ioNodes <- rawIoNodes.traverse(n => IoNode.fromNode(n))
        (inNodes, outNodes) <- splitIoNodes(ioNodes)
      yield (metadata, inNodes, outNodes, graphState)

  override def createConcreteNodes[R <: MapCacheLike[F]](
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[ConcreteNode[F]]],
      updateCache: List[ConcreteNode[F]] => F[R]
  ): F[(R, List[Node], List[ConcreteNode[F]])] = driver.transact(writeConf): tx =>
    for
      concreteNodes <- getAndIncrementNextHnIdQuery(numOfNodes)(tx).flatMap(ids => makeNodes(ids.map(HnId.apply)))
      params <- concreteNodes.traverse(n => n.toProperties.map(p => (n.ioNode.name, p)))
      rawNodes <- params.traverse((ioNodeName, props) => addConcreteNodeQuery(ioNodeName.value, props)(tx))
      newCache <- updateCache(concreteNodes)
    yield (newCache, rawNodes.flatten, concreteNodes)

  override def createAbstractNodes[R <: MapCacheLike[F]](
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[AbstractNode[F]]],
      updateCache: List[AbstractNode[F]] => F[R]
  ): F[(R, List[Node], List[AbstractNode[F]])] = driver.transact(writeConf): tx =>
    for
      abstractNodes <- getAndIncrementNextHnIdQuery(numOfNodes)(tx).flatMap(ids => makeNodes(ids.map(HnId.apply)))
      params <- abstractNodes.traverse(_.toProperties)
      rawNodes <- params.traverse(props => addAbstractNodeQuery(props)(tx))
      newCache <- updateCache(abstractNodes)
    yield (newCache, rawNodes, abstractNodes)

  override def findHiddenNodesByNames[R <: MapCacheLike[F]](
      names: List[Name],
      loadCached: List[HnId] => F[(R, List[HiddenNode[F]])],
      getIoNode: Name => F[IoNode[F]],
      updateCache: (R, List[HiddenNode[F]]) => F[R]
  ): F[(R, List[HiddenNode[F]])] = driver.transact(readConf): tx =>
    def loadAbstractNodes(ids: List[Long]): F[List[AbstractNode[F]]] =
      for
        nodes <- findAbstractNodesByIdsQuery(ids)(tx)
        abstractNodes <- nodes.traverse(n => AbstractNode.fromNode(n))
      yield abstractNodes

    def loadConcreteNodes(ids: List[Long]): F[List[ConcreteNode[F]]] =
      for
        nodes <- findConcreteNodesByIdsQuery(ids)(tx)
        withIoNames <- nodes.traverse:
          case hn :: ion :: Nil => IoNode.nameFromNode(ion).map(ioName => (hn, ioName))
          case ns               => s"Expected exactly 2 node but got: $ns".assertionError
        concreteNodes <- withIoNames
          .traverse((hn, ioName) => getIoNode(ioName).flatMap(ioNode => ConcreteNode.fromNode(hn, ioNode)))
      yield concreteNodes

    for
      ids <- findHiddenIdsNodesByNamesQuery(names.map(_.value))(tx).map(_.map(HnId.apply))
      _ <- ids.assertDistinct("Hidden node ids should be distinct")
      (cachedState, cachedNodes) <- loadCached(ids)
      notFoundIds = cachedNodes.map(_.id).diff(ids).map(_.value)
      abstractNodes <- loadAbstractNodes(notFoundIds)
      concreteNodes <- loadConcreteNodes(notFoundIds)
      allNodes = cachedNodes ++ abstractNodes ++ concreteNodes
      newCache <- updateCache(cachedState, allNodes)
    yield (newCache, allNodes)

  override def countHiddenNodes: F[Long] = driver.transact(readConf)(tx => countAllHiddenNodesQuery(tx))

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)
