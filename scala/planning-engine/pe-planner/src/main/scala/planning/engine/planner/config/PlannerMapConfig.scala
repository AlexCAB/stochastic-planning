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
| created: 2026-03-03 |||||||||||*/

package planning.engine.planner.config

import cats.effect.Sync
import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}

final case class PlannerMapConfig(
    reprEnabled: Boolean
)

object PlannerMapConfig:
  import pureconfig.generic.semiauto.*
  import pureconfig.module.catseffect.syntax.*

  def formConfig[F[_]: Sync](conf: Config): F[PlannerMapConfig] =
    given configReader: ConfigReader[PlannerMapConfig] = deriveReader[PlannerMapConfig]

    ConfigSource.fromConfig(conf).loadF[F, PlannerMapConfig]()
