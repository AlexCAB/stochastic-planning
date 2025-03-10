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
| created: 2025-03-10 |||||||||||*/


package planning.engine.config

import cats.effect.Sync
import com.typesafe.config.Config
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.module.catseffect.syntax._
import pureconfig.generic.semiauto._


final case class Neo4jConnectionConf(user: String, password: String, uri: String)

object Neo4jConnectionConf:
  def formConfig[F[_]: Sync](conf: Config): F[Neo4jConnectionConf] =
    given configReader: ConfigReader[Neo4jConnectionConf] = deriveReader[Neo4jConnectionConf]
    ConfigSource.fromConfig(conf).loadF[F, Neo4jConnectionConf]()
