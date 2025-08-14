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

package planning.engine.database

import cats.effect.Sync
import com.typesafe.config.Config
import org.neo4j.driver.{AuthToken, AuthTokens}
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.semiauto.*
import pureconfig.module.catseffect.syntax.*

final case class Neo4jConf(user: String, password: String, uri: String):
  lazy val authToken: AuthToken = AuthTokens.basic(user, password)

object Neo4jConf:
  def formConfig[F[_]: Sync](conf: => Config): F[Neo4jConf] =
    given configReader: ConfigReader[Neo4jConf] = deriveReader[Neo4jConf]

    ConfigSource.fromConfig(conf).loadF[F, Neo4jConf]()
