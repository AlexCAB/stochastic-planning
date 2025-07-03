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

package planning.engine.map.graph

import cats.effect.Async
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import cats.syntax.all.*
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.Sample
import planning.engine.map.subgraph.NextNodesMap

trait MapGraphLake[F[_]]:
  def metadata: MapMetadata
  def inputNodes: List[InputNode[F]]
  def outputNodes: List[OutputNode[F]]
  def getIoNode(name: Name): F[IoNode[F]]
  def newConcreteNodes(params: ConcreteNode.ListNew): F[List[HnId]]
  def newAbstractNodes(params: AbstractNode.ListNew): F[List[HnId]]
  def findHiddenNodesByNames(names: Set[Name]): F[Map[Name, Set[HiddenNode[F]]]]
  def findHnIdsByNames(names: Set[Name]): F[Map[Name, Set[HnId]]]
  def countHiddenNodes: F[Long]
  def addNewSamples(params: Sample.ListNew): F[List[SampleId]]
  def nextNodes(currentNodeId: HnId): F[NextNodesMap[F]]

class MapGraph[F[_]: {Async, LoggerFactory}](
    config: MapConfig,
    override val metadata: MapMetadata,
    override val inputNodes: List[InputNode[F]],
    override val outputNodes: List[OutputNode[F]],
    database: Neo4jDatabaseLike[F]
) extends MapGraphLake[F]:

  private val ioNodes = (inputNodes ++ outputNodes).map(n => (n.name, n)).toMap
  private val logger = LoggerFactory[F].getLogger

  assert(
    ioNodes.size == (inputNodes.size + outputNodes.size),
    "Input and output nodes must have unique names, ioNodes: $ioNodes"
  )

  override def getIoNode(name: Name): F[IoNode[F]] = ioNodes.get(name) match
    case Some(node) => node.pure
    case _          => s"Input node with name $name not found".assertionError

  override def newConcreteNodes(params: ConcreteNode.ListNew): F[List[HnId]] =
    for
      _ <- Validation.validateList(params.list)
      _ <- params.list.traverse(p => getIoNode(p.ioNodeName).map(_.variable.validateIndex(p.valueIndex)))
      hnIds <- database.createConcreteNodes(config.initNextHnIndex, params.list)
      _ <- logger.info(s"Created concrete nodes, hnIds = $hnIds, for params = $params")
    yield hnIds

  override def newAbstractNodes(params: AbstractNode.ListNew): F[List[HnId]] =
    for
      _ <- Validation.validateList(params.list)
      hnIds <- database.createAbstractNodes(config.initNextHnIndex, params.list)
      _ <- logger.info(s"Created abstract nodes, hnIds = $hnIds, for params = $params")
    yield hnIds

  override def findHiddenNodesByNames(names: Set[Name]): F[Map[Name, Set[HiddenNode[F]]]] =
    for
      foundHns <- database.findHiddenNodesByNames(names, name => getIoNode(name))
      _ <- logger.info(s"Found hidden nodes, foundHns = $foundHns, for names = $names")
    yield foundHns

  override def findHnIdsByNames(names: Set[Name]): F[Map[Name, Set[HnId]]] =
    for
      foundHnIds <- database.findHnIdsByNames(names)
      _ <- logger.info(s"Found hidden node IDs, foundHnIds = $foundHnIds, for names = $names")
    yield foundHnIds

  override def countHiddenNodes: F[Long] =
    for
      count <- database.countHiddenNodes
      _ <- logger.info(s"Counted total number of hidden nodes, count = $count")
    yield count

  override def addNewSamples(params: Sample.ListNew): F[List[SampleId]] =
    for
      _ <- Validation.validateList(params.list)
      (sampleIds, edgeIds) <- database.createSamples(params)
      _ <- logger.info(s"Added observed samples, sampleIds = $sampleIds, edgeIds = $edgeIds for params = $params")
    yield sampleIds

  override def nextNodes(currentNodeId: HnId): F[NextNodesMap[F]] = ???

object MapGraph:
  def apply[F[_]: {Async, LoggerFactory}](
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]],
      database: Neo4jDatabaseLike[F]
  ): F[MapGraph[F]] = new MapGraph[F](config, metadata, inNodes, outNodes, database).pure
