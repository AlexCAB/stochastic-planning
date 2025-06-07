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
import cats.effect.std.AtomicCell
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import cats.syntax.all.*
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.common.errors.*

trait MapGraphLake[F[_]]:
  def metadata: MapMetadata
  def inputNodes: List[InputNode[F]]
  def outputNodes: List[OutputNode[F]]
  def getState: F[MapCacheState[F]]
  def getIoNode(name: Name): F[IoNode[F]]
  def newConcreteNodes(params: List[ConcreteNode.New]): F[List[ConcreteNode[F]]]
  def newAbstractNodes(params: List[AbstractNode.New]): F[List[AbstractNode[F]]]
  def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]]
  def countHiddenNodes: F[Long]

class MapGraph[F[_]: {Async, LoggerFactory}](
    override val metadata: MapMetadata,
    override val inputNodes: List[InputNode[F]],
    override val outputNodes: List[OutputNode[F]],
    config: MapConfig,
    graphState: AtomicCell[F, MapCacheState[F]],
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

  override def getState: F[MapCacheState[F]] = graphState.get

  override def newConcreteNodes(params: List[ConcreteNode.New]): F[List[ConcreteNode[F]]] =
    def makeNodes(hnIds: List[HnId]): F[List[ConcreteNode[F]]] =
      for
        _ <- (params, hnIds).assertSameSize("Seems bug: Concrete node params and hnIds must have the same size")
        withIoNodes <- params.traverse(p => getIoNode(p.ioNodeName).map(n => (p, n)))
        nodes <- withIoNodes.zip(hnIds).traverse((ns, id) => ConcreteNode(id, ns._1.name, ns._2, ns._1.valueIndex))
      yield nodes

    graphState.evalModify(state =>
      for
        (nextState, rawNodes, concreteNodes) <- database
          .createConcreteNodes(params.size, makeNodes, state.toCache(config.maxCacheSize))
        _ <- logger.info(s"Created and touched Neo4j node: $rawNodes, concrete nodes: $concreteNodes")
      yield (nextState, concreteNodes)
    )

  override def newAbstractNodes(params: List[AbstractNode.New]): F[List[AbstractNode[F]]] =
    def makeNodes(hnIds: List[HnId]): F[List[AbstractNode[F]]] =
      for
        _ <- (params, hnIds).assertSameSize("Seems bug: Abstract node params and hnIds must have the same size")
        nodes <- params.zip(hnIds).traverse((p, id) => AbstractNode(id, p.name))
      yield nodes

    graphState.evalModify(state =>
      for
        (nextState, rawNodes, abstractNodes) <- database
          .createAbstractNodes(params.size, makeNodes, state.toCache(config.maxCacheSize))
        _ <- logger.info(s"Created and touched Neo4j node: $rawNodes, abstract nodes: $abstractNodes")
      yield (nextState, abstractNodes)
    )

  override def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]] = graphState.evalModify(state =>
    database
      .findHiddenNodesByNames(
        names,
        hnIds => state.findAndAllocateCached(hnIds),
        name => getIoNode(name),
        (st, nodes) => st.toCache(config.maxCacheSize)(nodes)
      )
  )

  override def countHiddenNodes: F[Long] = database.countHiddenNodes

object MapGraph:
  def apply[F[_]: {Async, LoggerFactory}](
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]],
      initCacheState: MapCacheState[F],
      database: Neo4jDatabaseLike[F]
  ): F[MapGraph[F]] =
    for
      state <- AtomicCell[F].of[MapCacheState[F]](initCacheState)
      graph = new MapGraph[F](metadata, inNodes, outNodes, config, state, database)
    yield graph
