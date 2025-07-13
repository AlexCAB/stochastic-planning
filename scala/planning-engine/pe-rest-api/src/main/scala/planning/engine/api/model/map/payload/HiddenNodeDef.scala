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

import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax.*
import planning.engine.map.hidden.node.{ConcreteNode, AbstractNode}

sealed trait HiddenNodeDef:
  def name: Name

final case class ConcreteNodeDef(name: Name, ioNodeName: Name, valueIndex: IoIndex) extends HiddenNodeDef:
  def toNew: ConcreteNode.New = ConcreteNode.New(
    name = Some(name),
    ioNodeName = ioNodeName,
    valueIndex = valueIndex
  )

final case class AbstractNodeDef(name: Name) extends HiddenNodeDef:
  def toNew: AbstractNode.New = AbstractNode.New(
    name = Some(name)
  )

object HiddenNodeDef:
  import io.circe.generic.auto.*
  import planning.engine.api.model.values.*

  given Encoder[HiddenNodeDef] = new Encoder[HiddenNodeDef]:
    final def apply(data: HiddenNodeDef): Json = data match
      case con: ConcreteNodeDef => Json.obj("type" -> Json.fromString("ConcreteNodeDef"), "data" -> con.asJson)
      case abs: AbstractNodeDef => Json.obj("type" -> Json.fromString("AbstractNodeDef"), "data" -> abs.asJson)

  given Decoder[HiddenNodeDef] = new Decoder[HiddenNodeDef]:
    final def apply(c: HCursor): Decoder.Result[HiddenNodeDef] =
      for
        tpe <- c.downField("type").as[String]
        data <- tpe match
          case "ConcreteNodeDef" => c.downField("data").as[ConcreteNodeDef]
          case "AbstractNodeDef" => c.downField("data").as[AbstractNodeDef]
      yield data
