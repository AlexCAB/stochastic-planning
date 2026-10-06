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

import io.circe.syntax.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import planning.engine.api.model.visualization.gsi.MapVisualizationMsg

trait VisualizationMsg

object VisualizationMsg:
  import io.circe.generic.auto.*, planning.engine.api.model.json.values.*

  private val typeKey: String = "type"
  private val dataKey: String = "data"

  private val mapMsgType: String = "MapVisualizationMsg"

  given Encoder[VisualizationMsg] =
    case mapMsg: MapVisualizationMsg => Json.obj(typeKey -> Json.fromString(mapMsgType), dataKey -> mapMsg.asJson)
    // TODO add other visualization message types here

  given Decoder[VisualizationMsg] = (c: HCursor) =>
    for
      tpe <- c.downField(typeKey).as[String]
      data <- tpe match
        case `mapMsgType` => c.downField(dataKey).as[MapVisualizationMsg]
        case t            => Left(io.circe.DecodingFailure(s"Unknown visualization message type: $t", c.history))
    yield data
