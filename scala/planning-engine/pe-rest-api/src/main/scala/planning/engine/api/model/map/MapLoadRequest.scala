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
| created: 2025-07-15 |||||||||||*/

package planning.engine.api.model.map

import planning.engine.common.values.db.DbName
import io.circe.{Encoder, Decoder}

final case class MapLoadRequest(dbName: DbName)

object MapLoadRequest:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.values.*

  implicit val decoder: Decoder[MapLoadRequest] = deriveDecoder[MapLoadRequest]
  implicit val encoder: Encoder[MapLoadRequest] = deriveEncoder[MapLoadRequest]
