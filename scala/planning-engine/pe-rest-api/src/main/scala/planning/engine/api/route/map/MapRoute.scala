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
import planning.engine.api.model.map.{MapAddSamplesRequest, MapInitRequest, MapLoadRequest}
import planning.engine.api.service.map.MapServiceLike
import cats.syntax.all.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.circe.*
import planning.engine.api.route.RouteBase

class MapRoute[F[_]: Concurrent](service: MapServiceLike[F]) extends RouteBase[F] with Http4sDsl[F]:
  import MapInitRequest.*

  val endpoints: HttpRoutes[F] = HttpRoutes.of[F]:
    case POST -> Root / "map" / "reset" => service.reset().response

    case req @ POST -> Root / "map" / "init" =>
      for
        request <- req.as[MapInitRequest]
        response <- service.init(request).response
      yield response

    case req @ POST -> Root / "map" / "load" =>
      for
        request <- req.as[MapLoadRequest]
        response <- service.load(request).response
      yield response

    case req @ POST -> Root / "map" / "samples" =>
      for
        request <- req.as[MapAddSamplesRequest]
        response <- service.addSamples(request).response
      yield response

object MapRoute:
  def apply[F[_]: Concurrent](service: MapServiceLike[F]): Resource[F, MapRoute[F]] =
    Resource.eval(MonadThrow[F].pure(new MapRoute[F](service)))
