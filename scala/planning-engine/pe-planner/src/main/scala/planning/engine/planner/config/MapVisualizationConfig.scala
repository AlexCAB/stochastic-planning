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
| created: 2025-12-27 |||||||||||*/

package planning.engine.planner.config

import cats.effect.Sync
import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}

import scala.concurrent.duration.FiniteDuration

final case class MapVisualizationConfig(
    enabled: Boolean,
    longPullTimeout: FiniteDuration
)

object MapVisualizationConfig:
  import pureconfig.generic.semiauto.*
  import pureconfig.module.catseffect.syntax.*

  def fromConfig[F[_]: Sync](conf: Config): F[MapVisualizationConfig] =
    given configReader: ConfigReader[MapVisualizationConfig] = deriveReader[MapVisualizationConfig]
    ConfigSource.fromConfig(conf).loadF[F, MapVisualizationConfig]()
