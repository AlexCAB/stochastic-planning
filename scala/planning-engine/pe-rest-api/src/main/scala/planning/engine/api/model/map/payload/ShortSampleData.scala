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
| created: 2025-07-08 |||||||||||*/

package planning.engine.api.model.map.payload

import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import io.circe.{Encoder, Decoder}

final case class ShortSampleData(
    id: SampleId,
    name: Option[Name]
)

object ShortSampleData:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.json.values.*

  implicit val decoder: Decoder[ShortSampleData] = deriveDecoder[ShortSampleData]
  implicit val encoder: Encoder[ShortSampleData] = deriveEncoder[ShortSampleData]
