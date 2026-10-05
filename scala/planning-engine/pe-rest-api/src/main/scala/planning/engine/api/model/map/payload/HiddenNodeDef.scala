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
| created: 2025-07-13 |||||||||||*/

package planning.engine.api.model.map.payload

import io.circe.syntax.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description

sealed trait HiddenNodeDef:
  def name: HnName

final case class ConcreteNodeDef(
    name: HnName,
    description: Option[Description],
    ioNodeName: IoName,
    value: Json,
) extends HiddenNodeDef

final case class AbstractNodeDef(
    name: HnName,
    description: Option[Description],
) extends HiddenNodeDef

object HiddenNodeDef:
  import io.circe.generic.auto.*
  import planning.engine.api.model.json.values.*

  given Encoder[HiddenNodeDef] =
    case con: ConcreteNodeDef => Json.obj("type" -> Json.fromString("ConcreteNode"), "data" -> con.asJson)
    case abs: AbstractNodeDef => Json.obj("type" -> Json.fromString("AbstractNode"), "data" -> abs.asJson)

  given Decoder[HiddenNodeDef] = (c: HCursor) =>
    for
      tpe <- c.downField("type").as[String]
      data <- tpe match
        case "ConcreteNode" => c.downField("data").as[ConcreteNodeDef]
        case "AbstractNode" => c.downField("data").as[AbstractNodeDef]
    yield data
