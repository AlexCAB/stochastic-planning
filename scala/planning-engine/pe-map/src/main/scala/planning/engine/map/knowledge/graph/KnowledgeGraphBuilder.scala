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
import planning.engine.common.errors.assertionError
import planning.engine.map.database.model.KnowledgeGraphDbData

trait KnowledgeGraphBuilderLike[F[_]]:
  def init(metadata: Metadata, inNodes: Vector[InputNode[F]], outNodes: Vector[OutputNode[F]]): F[KnowledgeGraphLake[F]]
  def load: F[KnowledgeGraphLake[F]]

class KnowledgeGraphBuilder[F[_]: {Async, LoggerFactory}](database: Neo4jDatabaseLike[F])
    extends KnowledgeGraphBuilderLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def nodesList(nodes: Vector[Node]): String =
    s"${nodes.map(n => s"${n.labels.mkString(":")}:${n.elementId}").mkString(", ")}"

  private def checkIoNodesNames(inNodes: Vector[InputNode[F]], outNodes: Vector[OutputNode[F]]): F[Unit] =
    val ioNames = (inNodes ++ outNodes).map(_.name).groupBy(identity).values
    if ioNames.forall(_.size == 1) then Sync[F].unit
    else s"Input nodes must have unique names, but found duplicates: ${ioNames.filter(_.size > 1)}".assertionError

  override def init(
      metadata: Metadata,
      inNodes: Vector[InputNode[F]],
      outNodes: Vector[OutputNode[F]]
  ): F[KnowledgeGraphLake[F]] =
    for
      _ <- checkIoNodesNames(inNodes, outNodes)
      samplesState <- Sync[F].pure(SamplesState.empty)
      graphState <- Sync[F].pure(KnowledgeGraphState.empty)
      createdNodes <- database
        .initDatabase(KnowledgeGraphDbData[F](metadata, inNodes, outNodes, samplesState, graphState))
      _ <- logger.info(s"Created nodes: ${nodesList(createdNodes)}")
      graph <- KnowledgeGraph[F](metadata, inNodes, outNodes, samplesState, graphState, database)
    yield graph

  override def load: F[KnowledgeGraphLake[F]] =
    for
      data <- database.loadRootNodes
      _ <- checkIoNodesNames(data.inNodes, data.outNodes)
      _ <- logger.info(s"Loaded metadata: $data")
      graph <- KnowledgeGraph[F]
        .apply(data.metadata, data.inNodes, data.outNodes, data.samplesState, data.graphState, database)
    yield graph

object KnowledgeGraphBuilder:
  def apply[F[_]: {Async, LoggerFactory}](database: Neo4jDatabaseLike[F]): Resource[F, KnowledgeGraphBuilder[F]] =
    Resource.eval(Sync[F].delay(new KnowledgeGraphBuilder[F](database)))
