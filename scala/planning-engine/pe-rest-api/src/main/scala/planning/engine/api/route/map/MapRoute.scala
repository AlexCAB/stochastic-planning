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
import cats.effect.Resource
import org.http4s.HttpRoutes
import org.http4s.dsl.io.{GET, POST, Root}
import planning.engine.api.service.maintenance.MaintenanceServiceLike
import org.http4s.dsl.Http4sDsl
import io.circe.syntax.EncoderOps
import cats.syntax.all.*
import io.circe.generic.auto.*
import org.http4s.circe.*

//TODO ....

class MapRoute[F[_]: MonadThrow](service: MapServiceLike[F]) extends Http4sDsl[F]:
  val mapRoute = HttpRoutes.of[F]:
    case req @ POST -> Root / "map" / "init" => 
      service.init(req.as[MapDefinition]).flatMap(info => Ok(info.asJson))

    case POST -> Root / "map" / "load" => 
      service.load.flatMap(info => Ok(info.asJson))

object MapRoute:
  def apply[F[_] : MonadThrow](service: MapServiceLike[F]): Resource[F, MapRoute[F]] =
    Resource.eval(MonadThrow[F].pure(new MapRoute[F](service)))
