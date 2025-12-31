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

package planning.engine.api.config

import cats.effect.Sync
import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}

final case class VisualizationServiceConf(
    mapEnabled: Boolean
)

object VisualizationServiceConf:
  import pureconfig.generic.semiauto.*
  import pureconfig.module.catseffect.syntax.*

  def fromConfig[F[_]: Sync](conf: Config): F[VisualizationServiceConf] =
    given configReader: ConfigReader[VisualizationServiceConf] = deriveReader[VisualizationServiceConf]
    ConfigSource.fromConfig(conf).loadF[F, VisualizationServiceConf]()
