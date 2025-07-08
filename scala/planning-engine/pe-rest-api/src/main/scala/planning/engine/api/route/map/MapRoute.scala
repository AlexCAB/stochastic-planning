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
import planning.engine.api.model.map.{MapAddSamplesRequest, MapInitRequest}
import planning.engine.api.service.map.MapServiceLike
import cats.syntax.all.*
import planning.engine.api.model.values.*

class MapRoute[F[_]: Concurrent](service: MapServiceLike[F]) extends Http4sDsl[F]:
  import io.circe.generic.auto.*
  import org.http4s.circe.*
  import MapInitRequest.*

  val endpoints: HttpRoutes[F] = HttpRoutes.of[F]:
    case req @ POST -> Root / "map" / "init" =>
      for
        definition <- req.as[MapInitRequest]
        info <- service.init(definition)
        res <- Ok(info.asJson)
      yield res

    case POST -> Root / "map" / "load" => service.load.flatMap(info => Ok(info.asJson))

    case req @ POST -> Root / "map" / "samples" =>
      for
        definition <- req.as[MapAddSamplesRequest]
        result <- service.addSamples(definition)
        res <- Ok(result.asJson)
      yield res

object MapRoute:
  def apply[F[_]: Concurrent](service: MapServiceLike[F]): Resource[F, MapRoute[F]] =
    Resource.eval(MonadThrow[F].pure(new MapRoute[F](service)))
