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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.config

import cats.effect.Sync
import com.typesafe.config.Config
import planning.engine.common.config.Neo4jConnectionConf
import cats.syntax.all.*

final case class DbConf(connection: Neo4jConnectionConf, name: String)

object DbConf:
  def formConfig[F[_]: Sync](conf: => Config): F[DbConf] =
    for
      connection <- Neo4jConnectionConf.formConfig[F](conf.getConfig("neo4j"))
      name <- Sync[F].delay(conf.getString("name"))
    yield DbConf(connection, name)
