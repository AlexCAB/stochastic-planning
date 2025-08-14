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
import cats.syntax.all.*
import planning.engine.database.Neo4jConf

final case class DbConf(connection: Neo4jConf)

object DbConf:
  def formConfig[F[_]: Sync](conf: => Config): F[DbConf] =
    for
        connection <- Neo4jConf.formConfig[F](conf.getConfig("neo4j"))
    yield DbConf(connection)
