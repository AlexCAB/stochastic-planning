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
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.samples.{Samples, SamplesState}
import cats.syntax.all.*

trait KnowledgeGraphLake[F[_]]:
  def metadata: Metadata
  def inputNodes: Vector[InputNode[F]]
  def outputNodes: Vector[OutputNode[F]]
  def samples: Samples[F]
  def countHiddenNodes: F[Long]

class KnowledgeGraph[F[_]: {Async, LoggerFactory}](
    override val metadata: Metadata,
    override val inputNodes: Vector[InputNode[F]],
    override val outputNodes: Vector[OutputNode[F]],
    override val samples: Samples[F],
    database: Neo4jDatabaseLike[F]
) extends KnowledgeGraphLake[F]:
  def countHiddenNodes: F[Long] = ???

object KnowledgeGraph:
  def apply[F[_]: {Async, LoggerFactory}](
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]],
      samplesState: SamplesState,
      database: Neo4jDatabaseLike[F]
  ): F[KnowledgeGraph[F]] =
    for
      samples <- Samples[F](samplesState, database)
      graph = new KnowledgeGraph[F](metadata, inNodes, outNodes, samples, database)
    yield graph
