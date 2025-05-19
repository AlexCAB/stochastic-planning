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

package planning.engine.map.knowledge.graph

import cats.effect.Async
import cats.effect.std.AtomicCell
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.samples.{Samples, SamplesState}
import cats.syntax.all.*
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}

trait KnowledgeGraphLake[F[_]]:
  def metadata: Metadata
  def inputNodes: List[InputNode[F]]
  def outputNodes: List[OutputNode[F]]
  def samples: Samples[F]
  def getState: F[KnowledgeGraphState[F]]
  def newConcreteNodes(params: List[(Option[Name], IoNode[F], IoIndex)]): F[List[ConcreteNode[F]]]
  def newAbstractNodes(params: List[Option[Name]]): F[List[AbstractNode[F]]]
  def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]]
  def releaseHiddenNodes(ids: List[HnId]): F[Unit]
  def countHiddenNodes: F[Long]

class KnowledgeGraph[F[_]: {Async, LoggerFactory}](
    override val metadata: Metadata,
    override val inputNodes: List[InputNode[F]],
    override val outputNodes: List[OutputNode[F]],
    override val samples: Samples[F],
    config: KnowledgeGraphConfig,
    graphState: AtomicCell[F, KnowledgeGraphState[F]],
    database: Neo4jDatabaseLike[F]
) extends KnowledgeGraphLake[F]:
  private val ioNodes = inputNodes ++ outputNodes
  private val logger = LoggerFactory[F].getLogger

  assert(
    ioNodes.map(_.name).distinct.size == ioNodes.size,
    "Input and output nodes must have unique names, ioNodes: $ioNodes"
  )

  override def getState: F[KnowledgeGraphState[F]] = graphState.get

  override def newConcreteNodes(params: List[(Option[Name], IoNode[F], IoIndex)]): F[List[ConcreteNode[F]]] =
    def addConcreteNodeLoop(nodes: List[ConcreteNode[F]]): F[Unit] = nodes match
      case Nil          => logger.info("All IO nodes updated")
      case node :: tail => node.init(addConcreteNodeLoop(tail)).map(_.void)

    graphState.evalModify(state =>
      for
        (nextState, nodes) <- state.addHiddenNode(params, p => ConcreteNode[F](state.nextHnIdId, p._1, p._2, p._3))
        dbParams <- nodes.map(n => n.toProperties.map(p => (n.ioNode.name, p))).sequence
        neo4jNodes <- database.createConcreteNodes(dbParams, addConcreteNodeLoop(nodes))
        _ <- logger.info(s"Created concrete and touched Neo4j node: $neo4jNodes")
      yield (nextState, nodes)
    )

  override def newAbstractNodes(params: List[Option[Name]]): F[List[AbstractNode[F]]] = graphState.evalModify(state =>
    for
      (nextState, nodes) <- state.addHiddenNode(params, p => AbstractNode[F](state.nextHnIdId, p))
      dbParams <- nodes.map(n => n.toProperties).sequence
      neo4jNodes <- database.createAbstractNodes(dbParams)
      _ <- logger.info(s"Created abstract Neo4j node: $neo4jNodes")
    yield (nextState, nodes)
  )

  override def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]] = graphState.evalModify(state =>
    database.findHiddenNodesByNames(
      names,
      (ids, loadFound) =>
        state.findAndAllocateCached(
          ids,
          notFoundIds =>
            loadFound(notFoundIds).flatMap(_
              .map(dbData =>
                IoNode
                  .findForNode(dbData._2, ioNodes)
                  .flatMap(ioNode => HiddenNode.fromNode(dbData._1, ioNode))
              )
              .sequence)
        )
    )
  )

  override def releaseHiddenNodes(ids: List[HnId]): F[Unit] = graphState
    .evalUpdate(_.releaseHns(ids, config.maxCacheSize).flatTap(_ => logger.info(s"Released nodes: $ids")))

  override def countHiddenNodes: F[Long] = database.countHiddenNodes

object KnowledgeGraph:
  private[map]  def apply[F[_]: {Async, LoggerFactory}](
      config: KnowledgeGraphConfig,
      metadata: Metadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]],
      samplesState: SamplesState,
      initState: KnowledgeGraphState[F],
      database: Neo4jDatabaseLike[F]
  ): F[KnowledgeGraph[F]] =
    for
      samples <- Samples[F](samplesState, database)
      state <- AtomicCell[F].of[KnowledgeGraphState[F]](initState)
      graph = new KnowledgeGraph[F](metadata, inNodes, outNodes, samples, config, state, database)
    yield graph
