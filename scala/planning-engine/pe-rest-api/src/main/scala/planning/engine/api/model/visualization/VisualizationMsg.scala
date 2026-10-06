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
  import io.circe.generic.auto.*,  planning.engine.api.model.json.values.*
  
  private val mapMsgType: String = "MapVisualizationMsg"

  given Encoder[VisualizationMsg] = new Encoder[VisualizationMsg]:
    final def apply(data: VisualizationMsg): Json = data match
      case mapMsg: MapVisualizationMsg => Json.obj("type" -> Json.fromString(mapMsgType), "data" -> mapMsg.asJson)
      
  given Decoder[VisualizationMsg] = new Decoder[VisualizationMsg]:
    final def apply(c: HCursor): Decoder.Result[VisualizationMsg] =
      for
        tpe <- c.downField("type").as[String]
        data <- tpe match
          case `mapMsgType` => c.downField("data").as[MapVisualizationMsg]
      yield data
