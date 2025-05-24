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
import planning.engine.common.values.node.{HnId, HnIndex, IoIndex}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.common.errors.assertionError

trait MapGraphLake[F[_]]:
  def metadata: MapMetadata
  def inputNodes: List[InputNode[F]]
  def outputNodes: List[OutputNode[F]]
  def getState: F[MapCacheState[F]]
  def getIoNode(name: Name): F[IoNode[F]]
  def newConcreteNodes(params: List[ConcreteNode.New]): F[List[ConcreteNode[F]]]
  def newAbstractNodes(params: List[AbstractNode.New]): F[List[AbstractNode[F]]]

//  def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]]
//  def releaseHiddenNodes(ids: List[HnId]): F[Unit]
//  def countHiddenNodes: F[Long]

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

  override def newConcreteNodes(params: List[ConcreteNode.New]): F[List[ConcreteNode[F]]] = graphState.evalModify(
    state =>
      for
        nodes <- params
          .map(p => getIoNode(p.ioNodeName).flatMap(n => ConcreteNode(state.nextHnIdId, p.name, n, p.valueIndex)))
          .sequence
        (neo4jNodes, nextState) <- database.createConcreteNodes(nodes, state.toCache(nodes, config.maxCacheSize))
        _ <- logger.info(s"Created concrete and touched Neo4j node: $neo4jNodes")
      yield (nextState, nodes)
  )

  override def newAbstractNodes(params: List[AbstractNode.New]): F[List[AbstractNode[F]]] =

    graphState.evalModify(state =>
      for
        (nextState, nodes) <- state.addHiddenNode(params, p => AbstractNode[F](state.nextHnIdId, p))
        dbParams <- nodes.map(n => n.toProperties).sequence
        neo4jNodes <- database.createAbstractNodes(dbParams)
        _ <- logger.info(s"Created abstract Neo4j node: $neo4jNodes")
      yield (nextState, nodes)
    )

//  override def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]] = graphState.evalModify(state =>
//    database.findHiddenNodesByNames(
//      names,
//      (ids, loadFound) =>
//        state.findAndAllocateCached(
//          ids,
//          notFoundIds =>
//            loadFound(notFoundIds).flatMap(_
//              .map(dbData =>
//                IoNode
//                  .findForNode(dbData._2, ioNodes)
//                  .flatMap(ioNode => HiddenNode.fromNode(dbData._1, ioNode))
//              )
//              .sequence)
//        )
//    )
//  )
//
//  override def releaseHiddenNodes(ids: List[HnId]): F[Unit] = graphState
//    .evalUpdate(_.releaseHns(ids, config.maxCacheSize).flatTap(_ => logger.info(s"Released nodes: $ids")))
//
//  override def countHiddenNodes: F[Long] = database.countHiddenNodes

object MapGraph:
  private[map] def apply[F[_]: {Async, LoggerFactory}](
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
