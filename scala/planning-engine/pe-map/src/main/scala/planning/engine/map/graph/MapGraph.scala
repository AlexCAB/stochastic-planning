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
import planning.engine.map.subgraph.NextNodesMap

trait MapGraphLake[F[_]]:
  def metadata: MapMetadata
  def inputNodes: List[InputNode[F]]
  def outputNodes: List[OutputNode[F]]
  def getIoNode(name: Name): F[IoNode[F]]
  def newConcreteNodes(params: List[ConcreteNode.New]): F[List[ConcreteNode[F]]]
  def newAbstractNodes(params: List[AbstractNode.New]): F[List[AbstractNode[F]]]
  def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]]
  def countHiddenNodes: F[Long]
  def nextNodes(currentNodeId: HnId): F[NextNodesMap[F]]

class MapGraph[F[_]: {Async, LoggerFactory}](
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

  override def newConcreteNodes(params: List[ConcreteNode.New]): F[List[ConcreteNode[F]]] =
    def makeNodes(hnIds: List[HnId]): F[List[ConcreteNode[F]]] =
      for
        _ <- (params, hnIds).assertSameSize("Seems bug: Concrete node params and hnIds must have the same size")
        withIoNodes <- params.traverse(p => getIoNode(p.ioNodeName).map(n => (p, n)))
        nodes = withIoNodes.zip(hnIds).map((ns, id) => ConcreteNode(id, ns._1.name, ns._2, ns._1.valueIndex))
      yield nodes

    for
      (rawNodes, concreteNodes) <- database
        .createConcreteNodes(params.size, makeNodes)
      _ <- logger.info(s"Created and touched Neo4j node: $rawNodes, concrete nodes: $concreteNodes")
    yield concreteNodes

  override def newAbstractNodes(params: List[AbstractNode.New]): F[List[AbstractNode[F]]] =
    def makeNodes(hnIds: List[HnId]): F[List[AbstractNode[F]]] =
      for
        _ <- (params, hnIds).assertSameSize("Seems bug: Abstract node params and hnIds must have the same size")
        nodes = params.zip(hnIds).map((p, id) => AbstractNode(id, p.name))
      yield nodes

    for
      (rawNodes, abstractNodes) <- database
        .createAbstractNodes(params.size, makeNodes)
      _ <- logger.info(s"Created and touched Neo4j node: $rawNodes, abstract nodes: $abstractNodes")
    yield abstractNodes

  override def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]] = database
    .findHiddenNodesByNames(
      names,
      name => getIoNode(name)
    )

  override def countHiddenNodes: F[Long] = database.countHiddenNodes

  override def nextNodes(currentNodeId: HnId): F[NextNodesMap[F]] = ???

object MapGraph:
  def apply[F[_]: {Async, LoggerFactory}](
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]],
      database: Neo4jDatabaseLike[F]
  ): F[MapGraph[F]] = new MapGraph[F](metadata, inNodes, outNodes, database).pure
