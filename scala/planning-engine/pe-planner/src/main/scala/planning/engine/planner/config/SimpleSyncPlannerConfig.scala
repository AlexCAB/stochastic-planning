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
| created: 2025-08-24 |||||||||||*/

package planning.engine.planner.config

import cats.effect.Sync
import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}

final case class SimpleSyncPlannerConfig(
    maxContextPathLength: Int
)

object SimpleSyncPlannerConfig:
  import pureconfig.generic.semiauto.*
  import pureconfig.module.catseffect.syntax.*

  def formConfig[F[_]: Sync](conf: Config): F[SimpleSyncPlannerConfig] =
    given configReader: ConfigReader[SimpleSyncPlannerConfig] = deriveReader[SimpleSyncPlannerConfig]
    ConfigSource.fromConfig(conf).loadF[F, SimpleSyncPlannerConfig]()
