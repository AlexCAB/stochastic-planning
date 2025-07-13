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

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.text.Name
import io.circe.{Encoder, Decoder}

final case class NewSampleEdge(
    sourceHnName: Name,
    targetHnName: Name,
    edgeType: EdgeType
)

object NewSampleEdge:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.values.*
  import planning.engine.api.model.enums.*

  implicit val decoder: Decoder[NewSampleEdge] = deriveDecoder[NewSampleEdge]
  implicit val encoder: Encoder[NewSampleEdge] = deriveEncoder[NewSampleEdge]
