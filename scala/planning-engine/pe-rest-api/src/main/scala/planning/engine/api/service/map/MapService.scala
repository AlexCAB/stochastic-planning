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
import planning.engine.api.model.map.{MapInfoResponse, MapInitRequest}
import cats.syntax.all.*
import planning.engine.map.graph.{MapBuilderLike, MapConfig, KnowledgeGraphLake}

trait MapServiceLike[F[_]]:
  def init(definition: MapInitRequest): F[MapInfoResponse]
  def load: F[MapInfoResponse]

class MapService[F[_]: {Async, LoggerFactory}](
                                                config: MapConfig,
                                                builder: MapBuilderLike[F],
                                                kgCell: AtomicCell[F, Option[KnowledgeGraphLake[F]]]
) extends MapServiceLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def initError(graph: KnowledgeGraphLake[F]): F[(Option[KnowledgeGraphLake[F]], MapInfoResponse)] =
    for
      _ <- logger.error(s"Knowledge graph already initialized, overwriting, $graph")
      err <- Async[F].raiseError[(Option[KnowledgeGraphLake[F]], MapInfoResponse)](
        new AssertionError("Knowledge graph already initialized")
      )
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

object MapService:
  def apply[F[_]: {Async, LoggerFactory}](
                                           config: MapConfig,
                                           builder: MapBuilderLike[F]
  ): Resource[F, MapService[F]] = Resource.eval(
    AtomicCell[F].of[Option[KnowledgeGraphLake[F]]](None)
      .map(kgCell => new MapService[F](config, builder, kgCell))
  )
