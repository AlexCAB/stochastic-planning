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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.service.map

import cats.effect.std.AtomicCell
import cats.effect.{Async, Resource}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.map.*
import cats.syntax.all.*
import planning.engine.map.graph.{MapBuilderLike, MapConfig, MapGraphLake}
import planning.engine.common.errors.assertionError

trait MapServiceLike[F[_]]:
  def init(definition: MapInitRequest): F[MapInfoResponse]
  def load: F[MapInfoResponse]
  def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse]

class MapService[F[_]: {Async, LoggerFactory}](
    config: MapConfig,
    builder: MapBuilderLike[F],
    kgCell: AtomicCell[F, Option[MapGraphLake[F]]]
) extends MapServiceLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def initError(graph: MapGraphLake[F]): F[(Option[MapGraphLake[F]], MapInfoResponse)] =
    for
      _ <- logger.error(s"Knowledge graph already initialized, overwriting, $graph")
      err <- "Knowledge graph already initialized".assertionError[F, (Option[MapGraphLake[F]], MapInfoResponse)]
    yield err

  override def init(definition: MapInitRequest): F[MapInfoResponse] = kgCell.evalModify:
    case None =>
      for
        metadata <- definition.toMetadata
        inputNodes <- definition.toInputNodes
        outputNodes <- definition.toOutputNodes
        knowledgeGraph <- builder.init(config, metadata, inputNodes, outputNodes)
        info <- MapInfoResponse.fromKnowledgeGraph(knowledgeGraph)
      yield (Some(knowledgeGraph), info)

    case Some(kg) => initError(kg)

  override def load: F[MapInfoResponse] = kgCell.evalModify:
    case None =>
      for
        knowledgeGraph <- builder.load(config)
        info <- MapInfoResponse.fromKnowledgeGraph(knowledgeGraph)
      yield (Some(knowledgeGraph), info)

    case Some(kg) => initError(kg)

  override def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse] = ???

object MapService:
  def apply[F[_]: {Async, LoggerFactory}](
      config: MapConfig,
      builder: MapBuilderLike[F]
  ): Resource[F, MapService[F]] = Resource.eval(
    AtomicCell[F].of[Option[MapGraphLake[F]]](None)
      .map(kgCell => new MapService[F](config, builder, kgCell))
  )
