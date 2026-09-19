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
import cats.syntax.all.*
import com.typesafe.config.{Config, ConfigFactory}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.parts.*
import planning.engine.map.config.MapConfig

final case class GsiMainWithDbConf(
    db: DbConf,
    server: ServerConf,
    mapGraph: MapConfig,
)

object GsiMainWithDbConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[GsiMainWithDbConf] =
    for
      dbConf <- DbConf.formConfig(appConf.getConfig("db"))
      serverConf <- ServerConf.formConfig(appConf.getConfig("api.server"))
      mapGraphConf <- MapConfig.formConfig(appConf.getConfig("map-graph"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield GsiMainWithDbConf(dbConf, serverConf, mapGraphConf)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, GsiMainWithDbConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
