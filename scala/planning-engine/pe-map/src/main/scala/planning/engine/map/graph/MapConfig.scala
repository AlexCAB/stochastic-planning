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
| created: 2025-05-18 |||||||||||*/

package planning.engine.map.graph

import com.typesafe.config.Config
import cats.effect.Sync
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.semiauto.*
import pureconfig.module.catseffect.syntax.*

final case class MapConfig(
    maxCacheSize: Long
)

object MapConfig:
  def formConfig[F[_]: Sync](conf: Config): F[MapConfig] =
    given configReader: ConfigReader[MapConfig] = deriveReader[MapConfig]
    ConfigSource.fromConfig(conf).loadF[F, MapConfig]()
