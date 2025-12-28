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
import cats.syntax.all.*
import planning.engine.planner.config.MapVisualizationConfig

final case class MainInMemConf(server: ServerConf, visualization: MapVisualizationConfig)

object MainInMemConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[MainInMemConf] =
    for
      serverConf <- ServerConf.formConfig(appConf.getConfig("api.server"))
      visualizationConf <- MapVisualizationConfig.fromConfig(appConf.getConfig("planner.map.visualization"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield MainInMemConf(serverConf, visualizationConf)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, MainInMemConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
