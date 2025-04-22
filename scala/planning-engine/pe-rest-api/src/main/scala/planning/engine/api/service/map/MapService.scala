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

import cats.ApplicativeThrow
import cats.effect.{Async, Resource}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.map.{MapDefinitionRequest, MapInfoResponse}

trait MapServiceLike[F[_]]:
  def init(definition: MapDefinitionRequest): F[MapInfoResponse]
  def load: F[MapInfoResponse]

class MapService[F[_]: {Async, LoggerFactory}] extends MapServiceLike[F]:
  private val logger = LoggerFactory[F].getLogger

  override def init(definition: MapDefinitionRequest): F[MapInfoResponse] =
    ApplicativeThrow[F].raiseError(new NotImplementedError("MapService.init not implemented"))

  override def load: F[MapInfoResponse] =
    ApplicativeThrow[F].raiseError(new NotImplementedError("MapService.load not implemented"))

object MapService:
  def apply[F[_]: {Async, LoggerFactory}](): Resource[F, MapService[F]] =
    Resource.eval(Async[F].delay(new MapService[F]))
