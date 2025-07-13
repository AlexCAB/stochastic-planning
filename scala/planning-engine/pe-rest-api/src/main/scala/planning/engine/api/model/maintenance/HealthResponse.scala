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
| created: 2025-04-19 |||||||||||*/

package planning.engine.api.model.maintenance

import planning.engine.api.model.enums.Status
import io.circe.{Encoder, Decoder}

final case class HealthResponse(status: Status, version: String)

object HealthResponse:
  import io.circe.generic.semiauto.*

  implicit val decoder: Decoder[HealthResponse] = deriveDecoder[HealthResponse]
  implicit val encoder: Encoder[HealthResponse] = deriveEncoder[HealthResponse]
