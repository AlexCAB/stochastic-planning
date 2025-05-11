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
| created: 2025-04-20 |||||||||||*/

package planning.engine.api.config

import cats.effect.Sync
import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.Config
import pureconfig.ConfigSource
import pureconfig.ConfigReader

import pureconfig.generic.semiauto.*
import pureconfig.module.catseffect.syntax.*
import planning.engine.api.config.ApiConfigReaders.given

final case class ServerConf(host: Host, port: Port, apiPrefix: String)

object ServerConf:
  def formConfig[F[_]: Sync](conf: Config): F[ServerConf] =
    given configReader: ConfigReader[ServerConf] = deriveReader[ServerConf]
    ConfigSource.fromConfig(conf).loadF[F, ServerConf]()
