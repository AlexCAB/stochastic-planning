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

package planning.engine.map.knowledge.graph

import com.typesafe.config.Config
import cats.effect.Sync
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.semiauto.*
import pureconfig.module.catseffect.syntax.*

final case class KnowledgeGraphConfig(
    maxCacheSize: Long
)

object KnowledgeGraphConfig:
  def formConfig[F[_]: Sync](conf: Config): F[KnowledgeGraphConfig] =
    given configReader: ConfigReader[KnowledgeGraphConfig] = deriveReader[KnowledgeGraphConfig]
    ConfigSource.fromConfig(conf).loadF[F, KnowledgeGraphConfig]()
