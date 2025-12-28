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
| created: 2025-12-29 |||||||||||*/

package planning.engine.api.model.visualization

final case class MapVisualizationMsg(
    m: String,
)

object MapVisualizationMsg:
  import io.circe.generic.semiauto.*
  import io.circe.{Encoder, Decoder}

  implicit val decoder: Decoder[MapVisualizationMsg] = deriveDecoder[MapVisualizationMsg]
  implicit val encoder: Encoder[MapVisualizationMsg] = deriveEncoder[MapVisualizationMsg]
