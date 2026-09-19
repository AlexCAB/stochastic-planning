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

package planning.engine.api.model.map

import io.circe.{Decoder, Encoder}
import planning.engine.api.model.map.payload.*
import planning.engine.common.values.db.DbName
import planning.engine.common.values.text.{Description, Name}

final case class MapInitRequest(
    dbName: DbName,
    name: Option[Name],
    description: Option[Description],
    inputNodes: List[IoNodeApiDef],
    outputNodes: List[IoNodeApiDef],
)

object MapInitRequest:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.json.values.*

  implicit val decoder: Decoder[MapInitRequest] = deriveDecoder[MapInitRequest]
  implicit val encoder: Encoder[MapInitRequest] = deriveEncoder[MapInitRequest]
