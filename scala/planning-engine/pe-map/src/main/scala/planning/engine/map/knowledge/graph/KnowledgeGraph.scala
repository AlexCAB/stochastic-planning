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
import planning.engine.common.values.node.IoIndex
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode, HiddenNode}
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.common.errors.assertDistinct

trait KnowledgeGraphLake[F[_]]:
  def metadata: Metadata
  def inputNodes: List[InputNode[F]]
  def outputNodes: List[OutputNode[F]]
  def samples: Samples[F]
  def getState: F[KnowledgeGraphState[F]]
  def newConcreteNodes(params: List[(Option[Name], IoNode[F], IoIndex)]): F[List[ConcreteNode[F]]]
  def newAbstractNodes(params: List[Option[Name]]): F[List[AbstractNode[F]]]
  def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]]
  def countHiddenNodes: F[Long]

private[map] trait KnowledgeGraphInternal[F[_]]:
  def releaseHiddenNode(node: HiddenNode[F]): F[Unit] // Remove the node from the graph cache

class KnowledgeGraph[F[_]: {Async, LoggerFactory}](
    override val metadata: Metadata,
    override val inputNodes: List[InputNode[F]],
    override val outputNodes: List[OutputNode[F]],
    override val samples: Samples[F],
    graphState: AtomicCell[F, KnowledgeGraphState[F]],
    database: Neo4jDatabaseLike[F]
) extends KnowledgeGraphLake[F] with KnowledgeGraphInternal[F]:
  (inputNodes ++ outputNodes).map(_.name).assertDistinct("Input nodes must have unique names, but found duplicates")
  
  private val logger = LoggerFactory[F].getLogger
  
  override def getState: F[KnowledgeGraphState[F]] = graphState.get

  override def newConcreteNodes(params: List[(Option[Name], IoNode[F], IoIndex)]): F[List[ConcreteNode[F]]] =
    graphState.evalModify(state =>
      for
        (nextState, concreteNodes) <- params.foldRight((state, List[ConcreteNode[F]]()).pure): 
          case ((name, ioNode, valueIndex), buffer) => 
            for
                (st, acc) <- buffer
                node <- ConcreteNode[F](st.nextHnIdId, name, ioNode, valueIndex, HiddenNodeState.init[F])
                nst <- st.addNewHn(node)
            yield (nst, node :: acc)
        dbParams <- concreteNodes.map(n => n.toDbParams.map(p => (n.ioNode.name, p))).sequence
        neo4jNodes <- database.createConcreteNodes(dbParams)         
    
            
           
            
            
        
        params <- ConcreteNode.makeDbParams(state.nextHnIdId, name, valueIndex)
        neo4jNodes <- database.createConcreteNode(ioNode.name, params)
        _ <- ioNode.addConcreteNode(node)
        _ <- logger.info(s"Created concrete and touched Neo4j node: $neo4jNodes")
        concreteNode <- ConcreteNode[F](state.nextHnIdId, name, ioNode, valueIndex, HiddenNodeState.init[F], this)
        nextState <- state.addNewHn(concreteNode)
      yield (nextState, concreteNode)
    )

  override def newAbstractNodes(params: List[Option[Name]]): F[List[AbstractNode[F]]] = graphState.evalModify(state =>
    for
      _ <- state.chekNextId
      params <- AbstractNode.makeDbParams[F](state.nextHnIdId, name)
      neo4jNode <- database.createAbstractNode(params)
      _ <- logger.info(s"Created abstract Neo4j node: $neo4jNode")
      abstractNode <- AbstractNode[F](state.nextHnIdId, name, HiddenNodeState.init[F], this)
      nextState <- state.addNewHn(abstractNode)
    yield (nextState, abstractNode)
  )

  override def findHiddenNodesByNames(names: List[Name]): F[List[HiddenNode[F]]] = graphState.evalModify(state =>
    database.findHiddenNodesByNames(
      names,
      (ids, loadFound) =>
        state.findAndAllocateCached(
          ids,
          notFoundIds => loadFound(notFoundIds).flatMap(_.map(HiddenNode.fromNode).sequence)
        )
    )
  )

  override def countHiddenNodes: F[Long] = database.countHiddenNodes

  override def releaseHiddenNode(node: HiddenNode[F]): F[Unit] = graphState
    .evalUpdate(_.removeHn(node).flatTap(_ => logger.info(s"Un-cached node: $node")))

object KnowledgeGraph:
  def apply[F[_]: {Async, LoggerFactory}](
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
      graph = new KnowledgeGraph[F](metadata, inNodes, outNodes, samples, state, database)
    yield graph
