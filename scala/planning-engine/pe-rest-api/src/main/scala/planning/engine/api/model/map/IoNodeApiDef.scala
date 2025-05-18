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

package planning.engine.api.model.map

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax.*
import io.circe.generic.auto.*

sealed trait IoNodeApiDef:
  val name: String

final case class BooleanIoNode(name: String, acceptableValues: Set[Boolean]) extends IoNodeApiDef
final case class FloatIoNode(name: String, min: Float, max: Float) extends IoNodeApiDef
final case class IntIoNode(name: String, min: Int, max: Int) extends IoNodeApiDef
final case class ListStrIoNode(name: String, elements: List[String]) extends IoNodeApiDef

object IoNodeApiDef:
  given Encoder[IoNodeApiDef] = new Encoder[IoNodeApiDef]:
    final def apply(data: IoNodeApiDef): Json = data match
      case bool: BooleanIoNode    => Json.obj("type" -> Json.fromString("BooleanIoNode"), "data" -> bool.asJson)
      case float: FloatIoNode     => Json.obj("type" -> Json.fromString("FloatIoNode"), "data" -> float.asJson)
      case int: IntIoNode         => Json.obj("type" -> Json.fromString("IntIoNode"), "data" -> int.asJson)
      case listStr: ListStrIoNode => Json.obj("type" -> Json.fromString("ListStrIoNode"), "data" -> listStr.asJson)

  given Decoder[IoNodeApiDef] = new Decoder[IoNodeApiDef]:
    final def apply(c: HCursor): Decoder.Result[IoNodeApiDef] =
      for
        tpe <- c.downField("type").as[String]
        data <- tpe match
          case "BooleanIoNode" => c.downField("data").as[BooleanIoNode]
          case "FloatIoNode"   => c.downField("data").as[FloatIoNode]
          case "IntIoNode"     => c.downField("data").as[IntIoNode]
          case "ListStrIoNode" => c.downField("data").as[ListStrIoNode]
      yield data
