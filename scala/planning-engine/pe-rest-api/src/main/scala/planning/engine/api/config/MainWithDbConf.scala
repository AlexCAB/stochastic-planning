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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.config

import cats.effect.{Resource, Sync}
import com.typesafe.config.{Config, ConfigFactory}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.config.MapConfig
import cats.syntax.all.*

final case class MainWithDbConf(
    db: DbConf,
    server: ServerConf,
    mapGraph: MapConfig
)

object MainWithDbConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[MainWithDbConf] =
    for
      dbConf <- DbConf.formConfig(appConf.getConfig("db"))
      serverConf <- ServerConf.formConfig(appConf.getConfig("api.server"))
      mapGraphConf <- MapConfig.formConfig(appConf.getConfig("map-graph"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield MainWithDbConf(dbConf, serverConf, mapGraphConf)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, MainWithDbConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
