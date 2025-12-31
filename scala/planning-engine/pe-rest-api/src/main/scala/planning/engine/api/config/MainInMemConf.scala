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
| created: 2025-12-23 |||||||||||*/

package planning.engine.api.config

import cats.effect.{Resource, Sync}
import com.typesafe.config.{Config, ConfigFactory}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.VisualizationServiceConf
import cats.syntax.all.*

final case class MainInMemConf(
    server: ServerConf,
    visRoute: VisualizationRouteConf,
    visService: VisualizationServiceConf
)

object MainInMemConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[MainInMemConf] =
    for
      serverConf <- ServerConf.formConfig(appConf.getConfig("api.server"))
      visRouteConf <- VisualizationRouteConf.fromConfig(appConf.getConfig("api.route.visualization"))
      visServiceConf <- VisualizationServiceConf.fromConfig(appConf.getConfig("api.service.visualization"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield MainInMemConf(serverConf, visRouteConf, visServiceConf)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, MainInMemConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
