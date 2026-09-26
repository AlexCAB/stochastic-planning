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
| created: 2026-09-16 |||||||||||*/

package planning.engine.api.config.mpi

import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import com.typesafe.config.{Config, ConfigFactory}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.parts.*

final case class MainInMemConf(
    server: ServerConf,
    visRoute: VisualizationRouteConf,
    visService: VisualizationServiceConf,
)

object MainInMemConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[MainInMemConf] =
    for
      server <- ServerConf.formConfig(appConf.getConfig("api.server"))
      visRoute <- VisualizationRouteConf.fromConfig(appConf.getConfig("api.route.visualization"))
      visService <- VisualizationServiceConf.fromConfig(appConf.getConfig("api.service.visualization"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield MainInMemConf(server, visRoute, visService)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, MainInMemConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
