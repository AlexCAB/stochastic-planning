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
import cats.syntax.all.*
import com.typesafe.config.{Config, ConfigFactory}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.parts.*
import planning.engine.planner.gsi.config.PlannerMapConfig

final case class GsiMainInMemConf(
    server: ServerConf,
    visRoute: VisualizationRouteConf,
    visService: VisualizationServiceConf,
    plannerMap: PlannerMapConfig,
)

object GsiMainInMemConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[GsiMainInMemConf] =
    for
      serverConf <- ServerConf.formConfig(appConf.getConfig("api.server"))
      visRouteConf <- VisualizationRouteConf.fromConfig(appConf.getConfig("api.route.visualization"))
      visServiceConf <- VisualizationServiceConf.fromConfig(appConf.getConfig("api.service.visualization"))
      plannerMap <- PlannerMapConfig.formConfig(appConf.getConfig("planner.map"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield GsiMainInMemConf(serverConf, visRouteConf, visServiceConf, plannerMap)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, GsiMainInMemConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
