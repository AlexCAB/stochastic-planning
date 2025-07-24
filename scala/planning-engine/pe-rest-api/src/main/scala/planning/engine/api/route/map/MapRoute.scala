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
| created: 2025-04-22 |||||||||||*/

package planning.engine.api.route.map

import cats.MonadThrow
import cats.effect.{Concurrent, Resource}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.syntax.EncoderOps
import planning.engine.api.model.map.{MapAddSamplesRequest, MapInitRequest, MapLoadRequest}
import planning.engine.api.service.map.MapServiceLike
import cats.syntax.all.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.*

class MapRoute[F[_]: Concurrent](service: MapServiceLike[F]) extends Http4sDsl[F]:
  import MapInitRequest.*

  val endpoints: HttpRoutes[F] = HttpRoutes.of[F]:
    case POST -> Root / "map" / "reset" =>
      for
        result <- service.reset()
        response <- Ok(result.asJson)
      yield response

    case req @ POST -> Root / "map" / "init" =>
      for
        request <- req.as[MapInitRequest]
        info <- service.init(request)
        response <- Ok(info.asJson)
      yield response

    case req @ POST -> Root / "map" / "load" =>
      for
        request <- req.as[MapLoadRequest]
        info <- service.load(request)
        response <- Ok(info.asJson)
      yield response

    case req @ POST -> Root / "map" / "samples" =>
      for
        request <- req.as[MapAddSamplesRequest]
        result <- service.addSamples(request)
        response <- Ok(result.asJson)
      yield response

object MapRoute:
  def apply[F[_]: Concurrent](service: MapServiceLike[F]): Resource[F, MapRoute[F]] =
    Resource.eval(MonadThrow[F].pure(new MapRoute[F](service)))
