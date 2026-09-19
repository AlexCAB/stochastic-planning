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

package planning.engine.api.config

import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import planning.engine.api.config.parts.*
import org.typelevel.log4cats.LoggerFactory
import com.typesafe.config.{Config, ConfigFactory}

final case class MpiMainInMemConf(
    server: ServerConf,
    visRoute: VisualizationRouteConf,
    visService: VisualizationServiceConf,
)

object MpiMainInMemConf:
  def formConfig[F[_] : {Sync, LoggerFactory}](appConf: Config): F[MpiMainInMemConf] =
    for
      server <- ServerConf.formConfig(appConf.getConfig("api.server"))
      visRoute <- VisualizationRouteConf.fromConfig(appConf.getConfig("api.route.visualization"))
      visService <- VisualizationServiceConf.fromConfig(appConf.getConfig("api.service.visualization"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield MpiMainInMemConf(server, visRoute, visService)

  def default[F[_] : {Sync, LoggerFactory}]: Resource[F, MpiMainInMemConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))