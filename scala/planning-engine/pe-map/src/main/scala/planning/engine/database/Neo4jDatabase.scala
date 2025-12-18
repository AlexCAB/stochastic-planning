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

package planning.engine.database

import cats.effect.Async
import neotypes.{AsyncDriver, AsyncTransaction, TransactionConfig}
import neotypes.model.types.Node
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.text.Name
import planning.engine.common.errors.*
import planning.engine.common.values.db.DbName
import planning.engine.common.values.db.Neo4j.{LINK_LABEL, THEN_LABEL}
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.config.MapConfig
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.map.samples.sample.{Sample, SampleData, SampleEdge}
import planning.engine.map.subgraph.{ConcreteWithParentIds, NextSampleEdge}

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
  def dbName: DbName
  def initDatabase(
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[List[Node]]

  def checkConnection: F[Long]
  def loadRootNodes: F[(MapMetadata, List[InputNode[F]], List[OutputNode[F]])]
  def createConcreteNodes(initNextHnIndex: Long, params: List[ConcreteNode.New]): F[Map[HnId, Option[HnName]]]
  def createAbstractNodes(initNextHnIndex: Long, params: List[AbstractNode.New]): F[Map[HnId, Option[HnName]]]
  def findHiddenNodesByNames(
      names: List[HnName],
      getIoNode: IoName => F[IoNode[F]]
  ): F[Map[HnName, List[HiddenNode[F]]]]

  def findHnIdsByNames(names: List[HnName]): F[Map[HnName, List[HnId]]]
  def countHiddenNodes: F[Long]
  def createSamples(params: Sample.ListNew): F[(List[Sample], List[String])]
  def countSamples: F[Long]
  def getNextSampleEdge(currentNodeId: HnId, getIoNode: IoName => F[IoNode[F]]): F[List[NextSampleEdge[F]]]
  def getSampleNames(sampleIds: List[SampleId]): F[Map[SampleId, Option[Name]]]
  def getSamplesData(sampleIds: List[SampleId]): F[Map[SampleId, SampleData]]
  def getSamples(sampleIds: List[SampleId]): F[Map[SampleId, Sample]]
  def findHiddenNodesByIoValues(values: List[(IoNode[F], IoIndex)]): F[List[ConcreteWithParentIds[F]]]

class Neo4jDatabase[F[_]: Async](driver: AsyncDriver[F], val dbName: DbName) extends Neo4jDatabaseLike[F]
    with Neo4jQueries:
  private val writeConf: TransactionConfig = TransactionConfig.default.withDatabase(dbName.value)
  private val readConf: TransactionConfig = TransactionConfig.readOnly.withDatabase(dbName.value)

  private def loadAbstractNodes(ids: List[Long])(tx: AsyncTransaction[F]): F[List[AbstractNode[F]]] =
    for
      nodes <- findAbstractNodesByIdsQuery(ids)(tx)
      abstractNodes <- nodes.traverse(n => AbstractNode.fromNode(n))
    yield abstractNodes

  private def loadConcreteNodes(
      ids: List[Long],
      getIoNode: IoName => F[IoNode[F]]
  )(tx: AsyncTransaction[F]): F[List[ConcreteNode[F]]] =
    for
      withIoNames <- findConcreteNodesByIdsQuery(ids)(tx).map(_.map((hn, ioName) => (hn, IoName(ioName))))
      concreteNodes <- withIoNames
        .traverse((hn, ioName) => getIoNode(ioName).flatMap(ioNode => ConcreteNode.fromNode(hn, ioNode)))
    yield concreteNodes

  override def toString: String = s"Neo4jDatabase(dbName = ${dbName.value})"

  override def checkConnection: F[Long] = driver.transact(readConf)(tx => checkConnectionQuery(tx))

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
  ): F[Map[HnId, Option[HnName]]] = driver.transact(writeConf): tx =>
    for
      newIds <- getNextHnIdQuery(params.size)(tx).map(_.map(HnId.apply))
      _ <- (newIds, params).assertSameSize("Created ids and given params must have the same size")
      params <- params.zip(newIds).traverse((n, id) => n.toProperties(id, initNextHnIndex).map(p => (n.ioNodeName, p)))
      createdIds <- params.traverse((ioNodeName, props) => addConcreteNodeQuery(ioNodeName.value, props)(tx))
      _ <- createdIds.map(_._1).assertDistinct("Concrete node ids should be distinct")
    yield createdIds.map((id, name) => HnId(id) -> name.map(HnName.apply)).toMap

  override def createAbstractNodes(
      initNextHnIndex: Long,
      params: List[AbstractNode.New]
  ): F[Map[HnId, Option[HnName]]] = driver.transact(writeConf): tx =>
    for
      newIds <- getNextHnIdQuery(params.size)(tx).map(_.map(HnId.apply))
      _ <- (newIds, params).assertSameSize("Created ids and given params must have the same size")
      params <- params.zip(newIds).traverse((n, id) => n.toProperties(id, initNextHnIndex))
      createdIds <- params.traverse(props => addAbstractNodeQuery(props)(tx))
      _ <- createdIds.map(_._1).assertDistinct("Abstract node ids should be distinct")
    yield createdIds.map((id, name) => HnId(id) -> name.map(HnName.apply)).toMap

  override def findHiddenNodesByNames(
      names: List[HnName],
      getIoNode: IoName => F[IoNode[F]]
  ): F[Map[HnName, List[HiddenNode[F]]]] = driver.transact(readConf): tx =>
    def buildMap(allNodes: List[HiddenNode[F]]): F[Map[HnName, List[HiddenNode[F]]]] = allNodes
      .foldRight(List[(HnName, HiddenNode[F])]().pure):
        case (node, acc) if node.name.nonEmpty => acc.map(nl => (node.name.get -> node) +: nl)
        case (node, _)                         => s"Seems bug, node expected to have name, node = $node".assertionError
      .map(_.groupBy(_._1).map((name, nodes) => (name, nodes.map(_._2))))

    for
      ids <- findHiddenIdsNodesByNamesQuery(names.map(_.value))(tx).map(_.map(_._2))
      _ <- ids.assertDistinct("Hidden node ids should be distinct")
      abstractNodes <- loadAbstractNodes(ids)(tx)
      concreteNodes <- loadConcreteNodes(ids, getIoNode)(tx)
      nodesMap <- buildMap(abstractNodes ++ concreteNodes)
    yield nodesMap

  override def findHnIdsByNames(names: List[HnName]): F[Map[HnName, List[HnId]]] = driver.transact(readConf): tx =>
    for
        ids <- findHiddenIdsNodesByNamesQuery(names.map(_.value))(tx)
    yield ids.map(r => HnName(r._1) -> HnId(r._2)).groupBy(_._1).view.mapValues(_.map(_._2)).toMap

  override def countHiddenNodes: F[Long] = driver.transact(readConf)(tx => countAllHiddenNodesQuery(tx))

  override def createSamples(params: Sample.ListNew): F[(List[Sample], List[String])] =
    driver.transact(writeConf): tx =>
      def storeSamples: F[List[(SampleId, Sample.New)]] =
        for
          newSampleId <- getNextSampleIdsQuery(params.list.size)(tx).map(_.map(SampleId.apply))
          _ <- (newSampleId, params.list).assertSameSize("Sample ids and observed samples must have the same size")
          params <- newSampleId.zip(params.list).traverse((id, s) => s.toQueryParams(id).map(p => (s, p)))
          storedSample <- params.traverse((s, p) => addSampleQuery(p)(tx).map(id => (SampleId(id), s)))
        yield storedSample

      def makeHnIndexies: F[Map[HnId, List[HnIndex]]] = params.numHnIndexPerHn.toSeq
        .traverse((id, c) => getNextHnIndexQuery(id.value, c)(tx).map(ins => id -> ins.reverse.map(HnIndex.apply)))
        .map(_.toMap)

      def addSampleEdge(
          samples: List[(SampleId, Sample.New)],
          hnInsToHnIndex: Map[HnId, List[HnIndex]]
      ): F[List[(SampleId, Sample.New, Map[HnId, HnIndex], List[String])]] = samples
        .foldRight((hnInsToHnIndex, List[(SampleId, Sample.New, Map[HnId, HnIndex], List[String])]()).pure):
          case ((sampleId, sample), accF) =>
            for
              (hnIndex, edgeIds) <- accF
              (newHnIndex, sampleIndexies) <- sample.findHnIndexies[F](hnIndex)
              edgesParams <- sample.edges.toSeq
                .traverse(e => e.toQueryParams(sampleId, sampleIndexies).map(p => (e, p)))
              ids <- edgesParams.traverse: (e, p) =>
                addSampleEdgeQuery(e.source.value, e.target.value, e.edgeType.toLabel, p._1, p._2)(tx)
            yield (newHnIndex, edgeIds :+ (sampleId, sample, sampleIndexies, ids))
        .map(_._2)

      for
        storedSamples <- storeSamples
        hnIndexies <- makeHnIndexies
        _ <- params.allEdges.traverse(e => addHiddenEdge(e.source.value, e.target.value, e.edgeType.toLabel)(tx))
        edgesAdded <- addSampleEdge(storedSamples, hnIndexies)
        edgeIds = edgesAdded.flatMap(_._4)
        _ <- updateNumberOfSamplesQuery(storedSamples.size)(tx)
        fullSamples <- edgesAdded.traverse((id, sample, indexies, _) => Sample.formNew(id, sample, indexies))
      yield (fullSamples, edgeIds)

  override def countSamples: F[Long] = driver.transact(readConf)(tx => countSamplesQuery(tx))

  override def getNextSampleEdge(currentNodeId: HnId, getIoNode: IoName => F[IoNode[F]]): F[List[NextSampleEdge[F]]] =
    driver.transact(readConf): tx =>
      def loadSamples(sampleIds: List[SampleId]): F[Map[SampleId, SampleData]] =
        for
          rawSamples <- getSamplesQuery(sampleIds.map(_.value))(tx)
          sampleData <- rawSamples.traverse(node => SampleData.fromNode(node))
        yield sampleData.map(sd => sd.id -> sd).toMap

      for
        rawEdges <- getNextEdgesQuery(currentNodeId.value)(tx)
        sampleEdges <- rawEdges.flatTraverse((edge, nId) => SampleEdge.fromEdge(currentNodeId, HnId(nId), edge))
        nextHnIds = rawEdges.map(_._2)
        sampleIds = sampleEdges.map(_.sampleId)
        abstractNodes <- loadAbstractNodes(nextHnIds)(tx)
        concreteNodes <- loadConcreteNodes(nextHnIds, getIoNode)(tx)
        sampleMap <- loadSamples(sampleIds)
        hnMap = (abstractNodes ++ concreteNodes).map(n => n.id -> n).toMap
        nextEdges <- sampleEdges.traverse(e => NextSampleEdge.fromSampleEdge(e, sampleMap, hnMap))
      yield nextEdges

  override def getSampleNames(sampleIds: List[SampleId]): F[Map[SampleId, Option[Name]]] = driver
    .transact(readConf): tx =>
      for
        rawNames <- getSampleNamesQuery(sampleIds.map(_.value))(tx)
        _ <- rawNames.map(_._1).assertDistinct("Sample Ids should be distinct")
      yield rawNames.map((id, name) => SampleId(id) -> name.map(Name.apply)).toMap

  override def getSamplesData(sampleIds: List[SampleId]): F[Map[SampleId, SampleData]] = driver
    .transact(readConf): tx =>
      for
        rawNodes <- getSamplesQuery(sampleIds.map(_.value))(tx)
        samples <- rawNodes.traverse(node => SampleData.fromNode(node))
        _ <- samples.map(_.id).assertDistinct("Sample Ids should be distinct")
      yield samples.map(sd => sd.id -> sd).toMap

  override def getSamples(sampleIds: List[SampleId]): F[Map[SampleId, Sample]] = driver
    .transact(readConf): tx =>
      for
        rawNodes <- getSamplesQuery(sampleIds.map(_.value))(tx)
        sampleDataMap <- rawNodes.traverse(node => SampleData.fromNode(node)).map(_.map(sd => sd.id -> sd).toMap)
        _ <- (sampleIds, sampleDataMap.keys).assertSameElems("Not for all sample the data were found")
        rawEdges <- getSampleEdgesQuery(sampleIds.map(_.toPropName))(tx).map(_.map((s, e, t) => (HnId(s), e, HnId(t))))
        edges <- sampleIds.traverse(sId => SampleEdge.fromEdgesBySampleId(rawEdges, sId).map(e => sId -> e))
        edgesMap = edges.groupBy(_._1).view.mapValues(_.flatMap(_._2)).toMap
        _ <- (sampleIds, edgesMap.keys).assertSameElems("Not for all sample the edges were found")
        samples <- sampleIds.traverse(sId => Sample.formDataMap(sId, sampleDataMap, edgesMap))
      yield samples.map(s => s.data.id -> s).toMap

  override def findHiddenNodesByIoValues(values: List[(IoNode[F], IoIndex)]): F[List[ConcreteWithParentIds[F]]] =
    driver.transact(readConf): tx =>
      values
        .traverse: (ioNode, ioIndex) =>
          findHiddenNodesByIoValueQuery(ioNode.name.value, ioIndex.value)(tx).flatMap: rawNodes =>
            rawNodes.traverse: rawNode =>
              for
                conNode <- ConcreteNode.fromNode(rawNode, ioNode)
                linkIds <- findParentIdsQuery(conNode.id.value, LINK_LABEL)(tx).map(_.map(HnId.apply))
                thenIds <- findParentIdsQuery(conNode.id.value, THEN_LABEL)(tx).map(_.map(HnId.apply))
              yield ConcreteWithParentIds(conNode, linkParentIds = linkIds.toSet, thenParentIds = thenIds.toSet)
        .map(_.flatten)

object Neo4jDatabase:
  def apply[F[_]: {Async, LoggerFactory}](driver: AsyncDriver[F], dbName: DbName): F[Neo4jDatabase[F]] =
    for
      db <- new Neo4jDatabase[F](driver, dbName).pure
      numOfNodes <- db.checkConnection
      _ <- LoggerFactory[F].getLogger.info(s"Initialized $db, total number of nodes: $numOfNodes")
    yield db
