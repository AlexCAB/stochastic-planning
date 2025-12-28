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

import io.circe.derivation.{Configuration, ConfiguredEnumCodec}
import planning.engine.api.model.maintenance.HealthResponse.Status
import io.circe.{Decoder, Encoder}

final case class HealthResponse(status: Status, version: String)

object HealthResponse:
  import io.circe.generic.semiauto.*

  implicit val decoder: Decoder[HealthResponse] = deriveDecoder[HealthResponse]
  implicit val encoder: Encoder[HealthResponse] = deriveEncoder[HealthResponse]

  given Configuration = Configuration.default

  enum Status derives ConfiguredEnumCodec:
    case OK, UNKNOWN, ERROR
