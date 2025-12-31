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
| created: 2025-12-31 |||||||||||*/

package planning.engine.api.config

import cats.effect.Sync
import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}

import scala.concurrent.duration.FiniteDuration

final case class VisualizationRouteConf(
    pingTimeout: FiniteDuration
)

object VisualizationRouteConf:
  import pureconfig.generic.semiauto.*
  import pureconfig.module.catseffect.syntax.*

  def fromConfig[F[_]: Sync](conf: Config): F[VisualizationRouteConf] =
    given configReader: ConfigReader[VisualizationRouteConf] = deriveReader[VisualizationRouteConf]
    ConfigSource.fromConfig(conf).loadF[F, VisualizationRouteConf]()
