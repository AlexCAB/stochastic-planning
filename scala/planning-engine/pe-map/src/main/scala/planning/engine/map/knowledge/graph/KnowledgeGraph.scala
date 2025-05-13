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
import planning.engine.common.values.{IoValueIndex, OpName}
import planning.engine.map.hidden.node.{ConcreteNode, AbstractNode}

trait KnowledgeGraphLake[F[_]]:
  def metadata: Metadata
  def inputNodes: Vector[InputNode[F]]
  def outputNodes: Vector[OutputNode[F]]
  def samples: Samples[F]

  def getState: F[KnowledgeGraphState]
  def countHiddenNodes: F[Long]

class KnowledgeGraph[F[_]: {Async, LoggerFactory}](
    override val metadata: Metadata,
    override val inputNodes: Vector[InputNode[F]],
    override val outputNodes: Vector[OutputNode[F]],
    override val samples: Samples[F],
    state: AtomicCell[F, KnowledgeGraphState],
    database: Neo4jDatabaseLike[F]
) extends KnowledgeGraphLake[F]:
  private val logger = LoggerFactory[F].getLogger

  private val ioNames = (inputNodes ++ outputNodes).map(_.name).groupBy(identity).values
  assert(
    ioNames.forall(_.size == 1),
    s"Input nodes must have unique names, but found duplicates: ${ioNames.filter(_.size != 1)}"
  )

  def getState: F[KnowledgeGraphState] = state.get

  def addConcreteNode(name: OpName, ioNode: IoNode[F], valueIndex: IoValueIndex): F[ConcreteNode[F]] = state.evalModify(s =>
    for
      params <- ConcreteNode.makeParameters[F](s.nextHiddenNodeId, name, valueIndex)
      neo4jNode <- database.createConcreteNode(ioNode.name, params)
      _ <- logger.info(s"Created node: $neo4jNode")
      concreteNode <- ConcreteNode[F](s.nextHiddenNodeId, name, valueIndex, ioNode)
    yield (s.withIncreasedId, concreteNode)
  )

  def addAbstractNode(name: OpName): F[AbstractNode[F]] = ???

  def countHiddenNodes: F[Long] = ???

object KnowledgeGraph:
  def apply[F[_]: {Async, LoggerFactory}](
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]],
      samplesState: SamplesState,
      initState: KnowledgeGraphState,
      database: Neo4jDatabaseLike[F]
  ): F[KnowledgeGraph[F]] =
    for
      samples <- Samples[F](samplesState, database)
      state <- AtomicCell[F].of[KnowledgeGraphState](initState)
      graph = new KnowledgeGraph[F](metadata, inNodes, outNodes, samples, state, database)
    yield graph
