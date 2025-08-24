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

package planning.engine.map

import cats.effect.{Async, Resource, Sync}
import cats.syntax.all.*
import neotypes.GraphDatabase
import neotypes.model.types.Node
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.errors.assertionError
import planning.engine.common.values.db.DbName
import planning.engine.database.{Neo4jConf, Neo4jDatabase, Neo4jDatabaseLike}
import planning.engine.map.config.MapConfig
import planning.engine.map.data.MapMetadata
import planning.engine.map.io.node.{InputNode, OutputNode}

trait MapBuilderLike[F[_]]:
  def init(
      dbName: DbName,
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[MapGraphLake[F]]

  def load(dbName: DbName, config: MapConfig): F[MapGraphLake[F]]

class MapBuilder[F[_]: {Async, LoggerFactory}](makeDb: DbName => F[Neo4jDatabaseLike[F]])
    extends MapBuilderLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def nodesList(nodes: List[Node]): String =
    s"${nodes.map(n => s"${n.labels.mkString(":")}:${n.elementId}").mkString(", ")}"

  private def checkIoNodesNames(inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[Unit] =
    val ioNames = (inNodes ++ outNodes).map(_.name).groupBy(identity).values
    if ioNames.forall(_.size == 1) then Sync[F].unit
    else s"Input nodes must have unique names, but found duplicates: ${ioNames.filter(_.size > 1)}".assertionError

  override def init(
      dbName: DbName,
      config: MapConfig,
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[MapGraphLake[F]] =
    for
      _ <- checkIoNodesNames(inNodes, outNodes)
      database <- makeDb(dbName)
      createdNodes <- database.initDatabase(config, metadata, inNodes, outNodes)
      _ <- logger.info(s"Created nodes: ${nodesList(createdNodes)}")
      graph <- MapGraph[F](config, metadata, inNodes, outNodes, database)
    yield graph

  override def load(dbName: DbName, config: MapConfig): F[MapGraphLake[F]] =
    for
      database <- makeDb(dbName)
      (metadata, inNodes, outNodes) <- database.loadRootNodes
      _ <- checkIoNodesNames(inNodes, outNodes)
      _ <- logger.info(s"Loaded metadata: ${(metadata, inNodes, outNodes)}")
      graph <- MapGraph[F](config, metadata, inNodes, outNodes, database)
    yield graph

object MapBuilder:
  import neotypes.cats.effect.implicits.*

  def apply[F[_]: {Async, LoggerFactory}](dbConfig: Neo4jConf): Resource[F, MapBuilder[F]] =
    for
        driver <- GraphDatabase.asyncDriver[F](dbConfig.uri, dbConfig.authToken)
    yield new MapBuilder[F](dbName => Neo4jDatabase[F](driver, dbName).map(db => db: Neo4jDatabaseLike[F]))
