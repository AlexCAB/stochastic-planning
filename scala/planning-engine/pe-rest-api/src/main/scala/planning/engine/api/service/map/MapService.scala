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
import planning.engine.api.model.map.{MapInitRequest, MapInfoResponse}
import planning.engine.core.map.knowledge.graph.{KnowledgeGraphLke, KnowledgeGraphBuilderLike}
import cats.syntax.all.*

trait MapServiceLike[F[_]]:
  def init(definition: MapInitRequest): F[MapInfoResponse]
  def load: F[MapInfoResponse]

class MapService[F[_]: {Async,
  LoggerFactory}](builder: KnowledgeGraphBuilderLike[F], kgCell: AtomicCell[F, Option[KnowledgeGraphLke[F]]])
    extends MapServiceLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def initError(graph: KnowledgeGraphLke[F]): F[(Option[KnowledgeGraphLke[F]], MapInfoResponse)] =
    for
      _ <- logger.error(s"Knowledge graph already initialized, overwriting, $graph")
      err <- Async[F].raiseError[(Option[KnowledgeGraphLke[F]], MapInfoResponse)](
        new AssertionError("Knowledge graph already initialized")
      )
    yield err

  override def init(definition: MapInitRequest): F[MapInfoResponse] = kgCell.evalModify:
    case None =>
      for
        metadata <- definition.toMetadata
        inputNodes <- definition.toInputNodes
        outputNodes <- definition.toOutputNodes
        knowledgeGraph <- builder.init(metadata, inputNodes, outputNodes)
        info <- MapInfoResponse.fromKnowledgeGraph(knowledgeGraph)
      yield (Some(knowledgeGraph), info)

    case Some(kg) => initError(kg)

  override def load: F[MapInfoResponse] = kgCell.evalModify:
    case None =>
      for
        knowledgeGraph <- builder.load
        info <- MapInfoResponse.fromKnowledgeGraph(knowledgeGraph)
      yield (Some(knowledgeGraph), info)

    case Some(kg) => initError(kg)

object MapService:
  def apply[F[_]: {Async, LoggerFactory}](builder: KnowledgeGraphBuilderLike[F]): Resource[F, MapService[F]] =
    Resource.eval(
      AtomicCell[F].of[Option[KnowledgeGraphLke[F]]](None)
        .map(kgCell => new MapService[F](builder, kgCell))
    )
