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
| created: 2025-04-25 |||||||||||*/

package planning.engine.map.knowledge.graph

import cats.effect.{Async, Resource, Sync}
import org.typelevel.log4cats.LoggerFactory
import cats.syntax.all.*
import neotypes.model.types.Node
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.samples.SamplesState

trait KnowledgeGraphBuilderLike[F[_]]:
  def init(metadata: Metadata, inNodes: Vector[InputNode[F]], outNodes: Vector[OutputNode[F]]): F[KnowledgeGraphLake[F]]
  def load: F[KnowledgeGraphLake[F]]

class KnowledgeGraphBuilder[F[_]: {Async, LoggerFactory}](database: Neo4jDatabaseLike[F])
    extends KnowledgeGraphBuilderLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def nodesList(nodes: Vector[Node]): String =
    s"${nodes.map(n => s"${n.labels.mkString(":")}:${n.elementId}").mkString(", ")}"

  override def init(
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]]
  ): F[KnowledgeGraphLake[F]] =
    for
      samplesState <- Sync[F].pure(SamplesState.empty)
      createdNodes <- database.initDatabase(metadata, inNodes, outNodes, samplesState)
      _ <- logger.info(s"Created nodes: ${nodesList(createdNodes)}")
      graph <- KnowledgeGraph[F](metadata, inNodes, outNodes, samplesState, database)
    yield graph

  override def load: F[KnowledgeGraphLake[F]] =
    for
      (metadata, inNodes, outNodes, samplesState) <- database.loadRootNodes
      _ <- logger.info(s"Loaded metadata: $metadata, in nodes: $inNodes, out nodes: $outNodes, samples: $samplesState")
      graph <- KnowledgeGraph[F](metadata, inNodes, outNodes, samplesState, database)
    yield graph

object KnowledgeGraphBuilder:
  def apply[F[_]: {Async, LoggerFactory}](database: Neo4jDatabaseLike[F]): Resource[F, KnowledgeGraphBuilder[F]] =
    Resource.eval(Sync[F].delay(new KnowledgeGraphBuilder[F](database)))
