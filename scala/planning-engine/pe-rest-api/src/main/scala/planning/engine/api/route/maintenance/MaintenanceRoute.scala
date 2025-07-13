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
| created: 2025-04-19 |||||||||||*/

package planning.engine.api.route.maintenance

import cats.MonadThrow
import cats.effect.Resource
import org.http4s.HttpRoutes
import planning.engine.api.service.maintenance.MaintenanceServiceLike
import org.http4s.dsl.Http4sDsl
import io.circe.syntax.EncoderOps
import cats.syntax.all.*
import planning.engine.api.model.maintenance.HealthResponse
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*

class MaintenanceRoute[F[_]: MonadThrow](service: MaintenanceServiceLike[F]) extends Http4sDsl[F]:
  import HealthResponse.*

  val endpoints: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / "maintenance" / "__health" => service.getHealth.flatMap(health => Ok(health.asJson))
    case POST -> Root / "maintenance" / "__exit"  => service.exit.flatMap(_ => Ok("Application terminated."))

object MaintenanceRoute:
  def apply[F[_]: MonadThrow](maintenanceService: MaintenanceServiceLike[F]): Resource[F, MaintenanceRoute[F]] =
    Resource.eval(MonadThrow[F].pure(new MaintenanceRoute[F](maintenanceService)))
