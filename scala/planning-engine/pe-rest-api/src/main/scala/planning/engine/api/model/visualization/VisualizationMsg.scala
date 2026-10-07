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
import planning.engine.api.model.visualization.gsi.GsiVisualizationMsg
import planning.engine.api.model.visualization.mpi.*

trait VisualizationMsg

object VisualizationMsg:
  import io.circe.generic.auto.*, planning.engine.api.model.json.values.*

  private val typeKey: String = "type"
  private val dataKey: String = "data"

  private val mapMsg: String = "MapVisualizationMsg"
  private val mpiInit: String = "MpiInit"
  private val mpiNewNodes: String = "MpiNewNodes"
  private val mpiNewEdges: String = "MpiNewEdges"

  given Encoder[VisualizationMsg] =
    case m: GsiVisualizationMsg => Json.obj(typeKey -> Json.fromString(mapMsg), dataKey -> m.asJson)
    case m: MpiInit             => Json.obj(typeKey -> Json.fromString(mpiInit), dataKey -> m.asJson)
    case m: MpiNewNodes         => Json.obj(typeKey -> Json.fromString(mpiNewNodes), dataKey -> m.asJson)
    case m: MpiNewEdges         => Json.obj(typeKey -> Json.fromString(mpiNewEdges), dataKey -> m.asJson)

  given Decoder[VisualizationMsg] = (c: HCursor) =>
    for
      tpe <- c.downField(typeKey).as[String]
      data <- tpe match
        case `mapMsg`      => c.downField(dataKey).as[GsiVisualizationMsg]
        case `mpiInit`     => c.downField(dataKey).as[MpiInit]
        case `mpiNewNodes` => c.downField(dataKey).as[MpiNewNodes]
        case `mpiNewEdges` => c.downField(dataKey).as[MpiNewEdges]
        case t             => Left(io.circe.DecodingFailure(s"Unknown visualization message type: $t", c.history))
    yield data
