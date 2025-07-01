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
import neotypes.{AsyncDriver, AsyncTransaction, GraphDatabase, TransactionConfig}
import neotypes.model.types.Node
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import neotypes.cats.effect.implicits.*
import cats.syntax.all.*
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.text.Name
import planning.engine.common.errors.*
import planning.engine.common.values.sample.SampleId
import planning.engine.map.graph.{MapConfig, MapMetadata}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.map.samples.sample.Sample

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
  def initDatabase(
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[List[Node]]

  def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]])]

  def createConcreteNodes(
      initNextHnIndex: Long,
      params: List[ConcreteNode.New]
  ): F[List[HnId]]

  def createAbstractNodes(initNextHnIndex: Long, params: List[AbstractNode.New]): F[List[HnId]]

  def findHiddenNodesByNames(
      names: List[Name],
      getIoNode: Name => F[IoNode[F]]
  ): F[List[HiddenNode[F]]]

  def countHiddenNodes: F[Long]

  def createSamples(params: Sample.ListNew): F[(List[SampleId], List[String])]

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], dbName: String) extends Neo4jDatabaseLike[F] with Neo4jQueries:
  private val writeConf: TransactionConfig = TransactionConfig.default.withDatabase(dbName)
  private val readConf: TransactionConfig = TransactionConfig.readOnly.withDatabase(dbName)

  override def initDatabase(
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[List[Node]] = driver.transact(writeConf): tx =>
    for
      mtParams <- metadata.toQueryParams
      rootConfParams <- config.toRootParams
      samplesConfParams <- config.toSamplesParams
      inParams <- inNodes.traverse(_.toQueryParams)
      outParams <- outNodes.traverse(_.toQueryParams)
      _ <- removeAllNodesQuery(tx)
      staticNodes <- createStaticNodesQuery(mtParams ++ rootConfParams, samplesConfParams)(tx)
      ioNodes <- (inParams ++ outParams).traverse((label, params) => createIoNodeQuery(label, params)(tx))
    yield staticNodes ++ ioNodes

  override def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]])] =
    def splitIoNodes(ioNodes: List[IoNode[F]]): F[(List[InputNode[F]], List[OutputNode[F]])] =
      ioNodes.foldRight((List[InputNode[F]](), List[OutputNode[F]]()).pure):
        case (inNode: InputNode[F], buf)   => buf.map((inNodes, outNodes) => (inNodes :+ inNode, outNodes))
        case (outNode: OutputNode[F], buf) => buf.map((inNodes, outNodes) => (inNodes, outNodes :+ outNode))
        case (node, _)                     => s"Invalid IoNode type: $node".assertionError

    driver.transact(readConf): tx =>
      for
        List(rootNode, _) <- readStaticNodesQuery(tx)
        metadata <- MapMetadata.fromNode(rootNode)
        rawIoNodes <- readIoNodesQuery(tx)
        ioNodes <- rawIoNodes.traverse(n => IoNode.fromNode(n))
        (inNodes, outNodes) <- splitIoNodes(ioNodes)
      yield (metadata, inNodes, outNodes)

  override def createConcreteNodes(
      initNextHnIndex: Long,
      params: List[ConcreteNode.New]
  ): F[List[HnId]] = driver.transact(writeConf): tx =>
    for
      newIds <- getNextHnIdQuery(params.size)(tx).map(_.map(HnId.apply))
      _ <- (newIds, params).assertSameSize("Created ids and given params must have the same size")
      params <- params.zip(newIds).traverse((n, id) => n.toProperties(id, initNextHnIndex).map(p => (n.ioNodeName, p)))
      createdIds <- params.traverse((ioNodeName, props) => addConcreteNodeQuery(ioNodeName.value, props)(tx))
    yield createdIds.map(HnId.apply)

  override def createAbstractNodes(initNextHnIndex: Long, params: List[AbstractNode.New]): F[List[HnId]] =
    driver.transact(writeConf): tx =>
      for
        newIds <- getNextHnIdQuery(params.size)(tx).map(_.map(HnId.apply))
        _ <- (newIds, params).assertSameSize("Created ids and given params must have the same size")
        params <- params.zip(newIds).traverse((n, id) => n.toProperties(id, initNextHnIndex))
        createdIds <- params.traverse(props => addAbstractNodeQuery(props)(tx))
      yield createdIds.map(HnId.apply)

  override def findHiddenNodesByNames(
      names: List[Name],
      getIoNode: Name => F[IoNode[F]]
  ): F[List[HiddenNode[F]]] = driver.transact(readConf): tx =>
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
      ids <- findHiddenIdsNodesByNamesQuery(names.map(_.value))(tx)
      _ <- ids.assertDistinct("Hidden node ids should be distinct")
      abstractNodes <- loadAbstractNodes(ids)
      concreteNodes <- loadConcreteNodes(ids)
      allNodes = abstractNodes ++ concreteNodes
    yield allNodes

  override def countHiddenNodes: F[Long] = driver.transact(readConf)(tx => countAllHiddenNodesQuery(tx))

  override def createSamples(params: Sample.ListNew): F[(List[SampleId], List[String])] =

    def storeSamples(tx: AsyncTransaction[F]): F[List[(SampleId, Sample.New)]] =
      for
        newSampleId <- getNextSamplesQuery(params.list.size)(tx).map(_.map(SampleId.apply))
        _ <- (newSampleId, params.list).assertSameSize("Sample ids and observed samples must have the same size")
        params <- newSampleId.zip(params.list).traverse((id, s) => s.toQueryParams(id).map(p => (s, p)))
        storedSample <- params.traverse((s, p) => addSampleQuery(p)(tx).map(id => (SampleId(id), s)))
      yield storedSample

    def makeHnIndexies(tx: AsyncTransaction[F]): F[Map[HnId, List[HnIndex]]] = params.numHnIndexPerHn
      .traverse((id, c) => getNextHnIndexQuery(id.value, c)(tx).map(ins => id -> ins.map(HnIndex.apply)))
      .map(_.toMap)

    def addSampleEdge(
        samples: List[(SampleId, Sample.New)],
        hnInsToHnIndex: Map[HnId, List[HnIndex]]
    )(tx: AsyncTransaction[F]): F[List[String]] = samples
      .foldRight((hnInsToHnIndex, List[String]()).pure):
        case ((sampleId, sample), acc) =>
          for
            (hnIndex, edgeIds) <- acc
            (newHnIndex, sampleIndexies) <- sample.findHnIndexies[F](hnIndex)
            edgesParams <- sample.edges.traverse(e => e.toQueryParams(sampleId, sampleIndexies).map(p => (e, p)))
            ids <- edgesParams.traverse: (e, p) =>
              addSampleEdgeQuery(e.source.value, e.target.value, e.edgeType.toLabel, p._1, p._2)(tx)
          yield (newHnIndex, edgeIds ++ ids)
      .map(_._2)

    driver.transact(readConf): tx =>
      for
        storedSamples <- storeSamples(tx)
        hnIndexies <- makeHnIndexies(tx)
        _ <- params.allEdges.traverse(e => addHiddenEdge(e.source.value, e.target.value, e.edgeType.toLabel)(tx))
        edgeIds <- addSampleEdge(storedSamples, hnIndexies)(tx)
        _ <- updateNumberOfSamplesQuery(storedSamples.size)(tx)
      yield (storedSamples.map(_._1), edgeIds)

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)
