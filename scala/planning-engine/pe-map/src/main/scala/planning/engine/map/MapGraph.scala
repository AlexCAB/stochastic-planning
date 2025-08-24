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
| created: 2025-03-15 |||||||||||*/

package planning.engine.map

import cats.effect.Async
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.database.Neo4jDatabaseLike
import planning.engine.map.config.MapConfig
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.samples.sample.{Sample, SampleData}
import planning.engine.map.subgraph.NextSampleEdgeMap

import scala.collection.immutable.Iterable

trait MapGraphLake[F[_]]:
  def metadata: MapMetadata
  def ioNodes: Map[Name, IoNode[F]]
  def getIoNode(name: Name): F[IoNode[F]]
  def newConcreteNodes(params: ConcreteNode.ListNew): F[Map[HnId, Option[Name]]]
  def newAbstractNodes(params: AbstractNode.ListNew): F[Map[HnId, Option[Name]]]
  def findHiddenNodesByNames(names: List[Name]): F[Map[Name, List[HiddenNode[F]]]]
  def findHnIdsByNames(names: List[Name]): F[Map[Name, List[HnId]]]
  def countHiddenNodes: F[Long]
  def addNewSamples(params: Sample.ListNew): F[List[SampleId]]
  def countSamples: F[Long]
  def nextSampleEdges(currentNodeId: HnId): F[NextSampleEdgeMap[F]]
  def getSampleNames(sampleIds: List[SampleId]): F[Map[SampleId, Option[Name]]]
  def getSamplesData(sampleIds: List[SampleId]): F[Map[SampleId, SampleData]]
  def getSamples(sampleIds: List[SampleId]): F[Map[SampleId, Sample]]
  def findConcreteNodesByIoValues(values: Map[Name, IoIndex]): F[List[ConcreteNode[F]]] // Name is IO variable name

class MapGraph[F[_]: {Async, LoggerFactory}](
    config: MapConfig,
    override val metadata: MapMetadata,
    override val ioNodes: Map[Name, IoNode[F]],
    database: Neo4jDatabaseLike[F]
) extends MapGraphLake[F]:

  private val logger = LoggerFactory[F].getLogger

  private def skipIfEmpty[A, R](list: Iterable[A], empty: => R)(block: => F[R]): F[R] =
    if list.isEmpty then empty.pure
    else block

  override def getIoNode(name: Name): F[IoNode[F]] = ioNodes.get(name) match
    case Some(node) => node.pure
    case _          => s"Input node with name $name not found".assertionError

  override def newConcreteNodes(params: ConcreteNode.ListNew): F[Map[HnId, Option[Name]]] =
    skipIfEmpty(params.list, Map[HnId, Option[Name]]()):
      for
        _ <- Validation.validateList(params.list)
        _ <- params.list.traverse(p => getIoNode(p.ioNodeName).map(_.variable.validateIndex(p.valueIndex)))
        hnIds <- database.createConcreteNodes(config.initNextHnIndex, params.list)
        _ <- logger.info(s"Created concrete nodes, hnIds = $hnIds, for params = $params")
      yield hnIds

  override def newAbstractNodes(params: AbstractNode.ListNew): F[Map[HnId, Option[Name]]] =
    skipIfEmpty(params.list, Map[HnId, Option[Name]]()):
      for
        _ <- Validation.validateList(params.list)
        hnIds <- database.createAbstractNodes(config.initNextHnIndex, params.list)
        _ <- logger.info(s"Created abstract nodes, hnIds = $hnIds, for params = $params")
      yield hnIds

  override def findHiddenNodesByNames(names: List[Name]): F[Map[Name, List[HiddenNode[F]]]] =
    skipIfEmpty(names, Map[Name, List[HiddenNode[F]]]()):
      for
        _ <- names.assertDistinct("Hidden nodes names must be distinct")
        foundHns <- database.findHiddenNodesByNames(names, getIoNode)
        _ <- logger.info(s"Found hidden nodes, foundHns = $foundHns, for names = $names")
        _ <- (names, foundHns.keys).assertSameElems("Not all hidden nodes were found")
      yield foundHns

  override def findHnIdsByNames(names: List[Name]): F[Map[Name, List[HnId]]] =
    skipIfEmpty(names, Map[Name, List[HnId]]()):
      for
        _ <- names.assertDistinct("Hidden nodes names must be distinct")
        foundHnIds <- database.findHnIdsByNames(names)
        _ <- logger.info(s"Found hidden node IDs, foundHnIds = $foundHnIds, for names = $names")
      yield foundHnIds

  override def countHiddenNodes: F[Long] =
    for
      count <- database.countHiddenNodes
      _ <- logger.info(s"Counted total number of hidden nodes, count = $count")
    yield count

  override def addNewSamples(params: Sample.ListNew): F[List[SampleId]] = skipIfEmpty(params.list, List[SampleId]()):
    for
      _ <- Validation.validateList(params.list)
      (sampleIds, edgeIds) <- database.createSamples(params)
      _ <- logger.info(s"Added observed samples, sampleIds = $sampleIds, edgeIds = $edgeIds for params = $params")
    yield sampleIds

  override def countSamples: F[Long] =
    for
      count <- database.countSamples
      _ <- logger.info(s"Counted total number of samples, count = $count")
    yield count

  override def nextSampleEdges(currentNodeId: HnId): F[NextSampleEdgeMap[F]] =
    for
      edges <- database.getNextSampleEdge(currentNodeId, getIoNode)
      _ <- logger.info(s"Got next sample edges, edges = $edges")
    yield NextSampleEdgeMap(currentNodeId, edges)

  override def getSampleNames(sampleIds: List[SampleId]): F[Map[SampleId, Option[Name]]] =
    skipIfEmpty(sampleIds, Map[SampleId, Option[Name]]()):
      for
        _ <- sampleIds.assertDistinct("Sample IDs must be distinct")
        sampleNames <- database.getSampleNames(sampleIds)
        _ <- logger.info(s"Got sample names, sampleNames = $sampleNames for sampleIds = $sampleIds")
        _ <- (sampleIds, sampleNames.keys).assertSameElems("Not all sample names were found")
      yield sampleNames

  override def getSamplesData(sampleIds: List[SampleId]): F[Map[SampleId, SampleData]] =
    skipIfEmpty(sampleIds, Map[SampleId, SampleData]()):
      for
        _ <- sampleIds.assertDistinct("Sample IDs must be distinct")
        samplesData <- database.getSamplesData(sampleIds)
        _ <- logger.info(s"Got samples data, sampleNames = $samplesData for sampleIds = $sampleIds")
        _ <- (sampleIds, samplesData.keys).assertSameElems("Not all sample data were found")
      yield samplesData

  override def getSamples(sampleIds: List[SampleId]): F[Map[SampleId, Sample]] =
    skipIfEmpty(sampleIds, Map[SampleId, Sample]()):
      for
        _ <- sampleIds.assertDistinct("Sample IDs must be distinct")
        samples <- database.getSamples(sampleIds)
        _ <- logger.info(s"Got samples = $samples for sampleIds = $sampleIds")
        _ <- (sampleIds, samples.keys).assertSameElems("Not all sample were found")
      yield samples

  override def findConcreteNodesByIoValues(values: Map[Name, IoIndex]): F[List[ConcreteNode[F]]] =
    skipIfEmpty(values, List[ConcreteNode[F]]()):
      for
        _ <- (ioNodes.keys, values.keys).assertContainsAll("Unknown IO nodes names")
        ioNodeWithIndex <- values.toList.traverse((n, i) => getIoNode(n).map(io => io -> i))
        foundNodes <- database.findHiddenNodesByIoValues(ioNodeWithIndex)
        _ <- logger.info(s"Found nodes = $foundNodes for values = $values")
        _ <- (values.keys, foundNodes.map(_.ioNode.name)).assertSameElems("Not all io nodes names was processed")
      yield foundNodes

  override def toString: String =
    s"MapGraph(name = ${metadata.name.toStr}, ioNodes = ${ioNodes.keys.map(_.value).mkString(", ")})"

object MapGraph:
  def apply[F[_]: {Async, LoggerFactory}](
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]],
      database: Neo4jDatabaseLike[F]
  ): F[MapGraph[F]] =
    for
      _ <- Validation.validateList(inNodes)
      _ <- Validation.validateList(outNodes)
      ioNodes = (inNodes ++ outNodes).map(n => n.name -> n)
      _ <- ioNodes.map(_._1).assertDistinct("Input and output nodes names must have unique names")
      _ <- LoggerFactory[F].getLogger.info(s"MapGraph built, connected to database: $database")
    yield new MapGraph[F](config, metadata, ioNodes.toMap, database)
