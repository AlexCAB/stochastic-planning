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

package planning.engine.api.model.map

import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import io.circe.{Encoder, Decoder}

final case class MapAddSamplesResponse(
    addedSamples: List[ShortSampleData]
)

object MapAddSamplesResponse:
  import io.circe.generic.semiauto.*

  implicit val decoder: Decoder[MapAddSamplesResponse] = deriveDecoder[MapAddSamplesResponse]
  implicit val encoder: Encoder[MapAddSamplesResponse] = deriveEncoder[MapAddSamplesResponse]

  def fromSampleNames(sampleNames: Map[SampleId, Option[Name]]): MapAddSamplesResponse =
    MapAddSamplesResponse(sampleNames.map((id, name) => ShortSampleData(id, name)).toList)
