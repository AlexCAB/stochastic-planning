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
import neotypes.model.types.{Node, Relationship}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import neotypes.cats.effect.implicits.*
import cats.syntax.all.*
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.text.Name
import planning.engine.common.errors.*
import planning.engine.common.values.sample.SampleId
import planning.engine.map.graph.{MapConfig, MapMetadata}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.map.samples.sample.observed.{ObservedEdge, ObservedSample}
import planning.engine.map.samples.sample.stored.{SampleEdge, StoredSample}

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
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[ConcreteNode[F]]],
      initNextHnIndex: Long
  ): F[(List[Node], List[ConcreteNode[F]])]

  def createAbstractNodes(
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[AbstractNode[F]]],
      initNextHnIndex: Long
  ): F[(List[Node], List[AbstractNode[F]])]

  def findHiddenNodesByNames(
      names: List[Name],
      getIoNode: Name => F[IoNode[F]]
  ): F[List[HiddenNode[F]]]

  def countHiddenNodes: F[Long]

  def addObservedSamples(
      observedSamples: List[ObservedSample],
      makeStoredSample: (ObservedSample, SampleId, Map[HnId, HnIndex]) => F[StoredSample]
  ): F[(List[Node], List[Relationship], List[StoredSample])]

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
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[ConcreteNode[F]]],
      initNextHnIndex: Long
  ): F[(List[Node], List[ConcreteNode[F]])] = driver.transact(writeConf): tx =>
    for
      _ <- numOfNodes.assetAnNumberOf("numOfNodes")
      concreteNodes <- getNextHnIdQuery(numOfNodes)(tx).flatMap(ids => makeNodes(ids.map(HnId.apply)))
      params <- concreteNodes.traverse(n => n.toProperties(initNextHnIndex).map(p => (n.ioNode.name, p)))
      rawNodes <- params.traverse((ioNodeName, props) => addConcreteNodeQuery(ioNodeName.value, props)(tx))
    yield (rawNodes.flatten, concreteNodes)

  override def createAbstractNodes(
      numOfNodes: Long,
      makeNodes: List[HnId] => F[List[AbstractNode[F]]],
      initNextHnIndex: Long
  ): F[(List[Node], List[AbstractNode[F]])] = driver.transact(writeConf): tx =>
    for
      _ <- numOfNodes.assetAnNumberOf("numOfNodes")
      abstractNodes <- getNextHnIdQuery(numOfNodes)(tx).flatMap(ids => makeNodes(ids.map(HnId.apply)))
      params <- abstractNodes.traverse(_.toProperties(initNextHnIndex))
      rawNodes <- params.traverse(props => addAbstractNodeQuery(props)(tx))
    yield (rawNodes, abstractNodes)

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

  override def addObservedSamples(
      observedSamples: List[ObservedSample],
      makeStoredSample: (ObservedSample, SampleId, Map[HnId, HnIndex]) => F[StoredSample]
  ): F[(List[Node], List[Relationship], List[StoredSample])] =
    def aggregateEdgesAndNodeIds: F[(List[ObservedEdge], List[HnId])] =
      (observedSamples.flatMap(_.edges).distinct, observedSamples.flatMap(_.hnIds).distinct).pure

    def makeSampleWithIds(tx: AsyncTransaction[F]): F[List[(SampleId, ObservedSample)]] =
      for
        sampleIds <- getNextSamplesQuery(observedSamples.size)(tx).map(_.map(SampleId.apply))
        _ <- (sampleIds, observedSamples).assertSameSize("Sample ids and observed samples must have the same size")
      yield sampleIds.zip(observedSamples)

    def makeHnIndexies(allNodeIds: List[HnId])(tx: AsyncTransaction[F]): F[Map[HnId, List[HnIndex]]] = allNodeIds
      .map(id => id -> observedSamples.count(_.hnIds.contains(id)))
      .traverse((id, c) => getNextHnIndexQuery(id.value, c)(tx).map(ins => (id, ins.map(HnIndex.apply))))
      .map(_.toMap)

    def getHnIndexiesForSample(hnIns: Map[HnId, List[HnIndex]]): F[(Map[HnId, List[HnIndex]], Map[HnId, HnIndex])] =
      hnIns.foldRight((Map[HnId, List[HnIndex]](), Map[HnId, HnIndex]()).pure):
        case ((hnId, hnIx :: hnIxs), acc) => acc.map((ixs, ix) => (ixs + (hnId -> hnIxs), ix + (hnId -> hnIx)))
        case ((hnId, Nil), _)             => s"Incorrect number of HnIndex created for hnId = $hnId".assertionError

    def makeStoredSamples(
        samples: List[(SampleId, ObservedSample)],
        hnIns: Map[HnId, List[HnIndex]]
    ): F[List[StoredSample]] = samples
      .foldRight((hnIns, List[StoredSample]()).pure):
        case ((sId, sample), acc) =>
          for
            (hnIns, sss) <- acc
            (nHnIns, ixs) <- getHnIndexiesForSample(hnIns)
            ss <- makeStoredSample(sample, sId, ixs)
          yield (nHnIns, ss +: sss)
      .map(_._2)

    def addSampleEdge(edges: Seq[SampleEdge])(tx: AsyncTransaction[F]): F[List[Relationship]] =
      for
        edgesParams <- edges
          .map(e => e -> e.toParams)
          .map((e, p) => (e.sourceHn.value, e.targetHn.value, e.edgeType.toLabel, p._1, p._2))
          .pure
        rawEdges <- edgesParams.traverse:
          case (sId, tId, label, pn, pv) => addSampleEdgeQuery(sId, tId, label, pn, pv)(tx)
      yield rawEdges.toList

    driver.transact(readConf): tx =>
      for
        (allEdges, allNodeIds) <- aggregateEdgesAndNodeIds
        samplesWithIds <- makeSampleWithIds(tx)
        hnIndexies <- makeHnIndexies(allNodeIds)(tx)
        storedSamples <- makeStoredSamples(samplesWithIds, hnIndexies)
        _ <- allEdges.traverse(e => addEdgeIfNotExistQuery(e.source.value, e.target.value, e.edgeType.toLabel)(tx))
        rawNodes <- storedSamples.traverse(_.toQueryParams.flatMap(p => addSampleQuery(p)(tx)))
        rawEdges <- addSampleEdge(storedSamples.flatMap(_.edges))(tx)
        _ <- updateNumberOfSamplesQuery(storedSamples.size)(tx)
      yield (rawNodes, rawEdges, storedSamples)

object Neo4jDatabase:
  def apply[F[_]: Async](connectionConf: Neo4jConf, dbName: String): Resource[F, Neo4jDatabase[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](connectionConf.uri, connectionConf.authToken)
    yield new Neo4jDatabase[F](driver, dbName)
