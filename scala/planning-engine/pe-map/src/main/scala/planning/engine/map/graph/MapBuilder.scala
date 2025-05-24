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

package planning.engine.map.graph

import cats.effect.{Async, Resource, Sync}
import org.typelevel.log4cats.LoggerFactory
import cats.syntax.all.*
import neotypes.model.types.Node
import planning.engine.map.database.Neo4jDatabaseLike
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.common.errors.assertionError

trait MapBuilderLike[F[_]]:
  def init(
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[MapGraphLake[F]]

  def load(config: MapConfig): F[MapGraphLake[F]]

class MapBuilder[F[_]: {Async, LoggerFactory}](database: Neo4jDatabaseLike[F])
    extends MapBuilderLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def nodesList(nodes: List[Node]): String =
    s"${nodes.map(n => s"${n.labels.mkString(":")}:${n.elementId}").mkString(", ")}"

  private def checkIoNodesNames(inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[Unit] =
    val ioNames = (inNodes ++ outNodes).map(_.name).groupBy(identity).values
    if ioNames.forall(_.size == 1) then Sync[F].unit
    else s"Input nodes must have unique names, but found duplicates: ${ioNames.filter(_.size > 1)}".assertionError

  override def init(
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[MapGraphLake[F]] =
    for
      _ <- checkIoNodesNames(inNodes, outNodes)
      graphState <- MapCacheState.init
      createdNodes <- database.initDatabase(metadata, inNodes, outNodes)
      _ <- logger.info(s"Created nodes: ${nodesList(createdNodes)}")
      graph <- MapGraph[F](config, metadata, inNodes, outNodes, graphState, database)
    yield graph

  override def load(config: MapConfig): F[MapGraphLake[F]] =
    for
      (metadata, inNodes, outNodes, graphState) <- database.loadRootNodes
      _ <- checkIoNodesNames(inNodes, outNodes)
      _ <- logger.info(s"Loaded metadata: ${(metadata, inNodes, outNodes, graphState)}")
      graph <- MapGraph[F](config, metadata, inNodes, outNodes, graphState, database)
    yield graph

object MapBuilder:
  def apply[F[_]: {Async, LoggerFactory}](database: Neo4jDatabaseLike[F]): Resource[F, MapBuilder[F]] =
    Resource.eval(Sync[F].delay(new MapBuilder[F](database)))
