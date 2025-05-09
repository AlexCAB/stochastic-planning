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
import cats.syntax.all.*

final case class MainConf(db: DbConf, server: ServerConf)

object MainConf:
  def formConfig[F[_]: {Sync, LoggerFactory}](appConf: Config): F[MainConf] =
    for
      dbConf <- DbConf.formConfig(appConf.getConfig("db"))
      serverConf <- ServerConf.formConfig(appConf.getConfig("api.server"))
      _ <- LoggerFactory[F].getLogger.info(s"Loaded configuration: $appConf")
    yield MainConf(dbConf, serverConf)

  def default[F[_]: {Sync, LoggerFactory}]: Resource[F, MainConf] =
    Resource.eval(Sync[F].delay(ConfigFactory.load()).flatMap(ac => formConfig[F](ac)))
