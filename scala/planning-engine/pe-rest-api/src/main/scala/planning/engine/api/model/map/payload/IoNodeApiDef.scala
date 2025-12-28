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
| created: 2025-04-27 |||||||||||*/

package planning.engine.api.model.map.payload

import io.circe.syntax.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import planning.engine.api.model.map.*
import planning.engine.common.values.io.IoName

sealed trait IoNodeApiDef:
  val name: IoName

final case class BooleanIoNodeDef(name: IoName, acceptableValues: Set[Boolean]) extends IoNodeApiDef
final case class FloatIoNodeDef(name: IoName, min: Float, max: Float) extends IoNodeApiDef
final case class IntIoNodeDef(name: IoName, min: Int, max: Int) extends IoNodeApiDef
final case class ListStrIoNodeDef(name: IoName, elements: List[String]) extends IoNodeApiDef

object IoNodeApiDef:
  import io.circe.generic.auto.*
  import planning.engine.api.model.json.values.*

  given Encoder[IoNodeApiDef] = new Encoder[IoNodeApiDef]:
    final def apply(data: IoNodeApiDef): Json = data match
      case bool: BooleanIoNodeDef    => Json.obj("type" -> Json.fromString("BooleanIoNode"), "data" -> bool.asJson)
      case float: FloatIoNodeDef     => Json.obj("type" -> Json.fromString("FloatIoNode"), "data" -> float.asJson)
      case int: IntIoNodeDef         => Json.obj("type" -> Json.fromString("IntIoNode"), "data" -> int.asJson)
      case listStr: ListStrIoNodeDef => Json.obj("type" -> Json.fromString("ListStrIoNode"), "data" -> listStr.asJson)

  given Decoder[IoNodeApiDef] = new Decoder[IoNodeApiDef]:
    final def apply(c: HCursor): Decoder.Result[IoNodeApiDef] =
      for
        tpe <- c.downField("type").as[String]
        data <- tpe match
          case "BooleanIoNode" => c.downField("data").as[BooleanIoNodeDef]
          case "FloatIoNode"   => c.downField("data").as[FloatIoNodeDef]
          case "IntIoNode"     => c.downField("data").as[IntIoNodeDef]
          case "ListStrIoNode" => c.downField("data").as[ListStrIoNodeDef]
      yield data
