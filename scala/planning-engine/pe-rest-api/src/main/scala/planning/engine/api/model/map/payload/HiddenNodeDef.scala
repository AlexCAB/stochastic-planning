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

import cats.MonadThrow
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.text.Description
import io.circe.{Decoder, Encoder, HCursor, Json}
import cats.syntax.all.*
import io.circe.syntax.*
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.HnName
import planning.engine.map.io.variable.*

sealed trait HiddenNodeDef:
  def name: HnName

final case class ConcreteNodeDef(name: HnName, description: Option[Description], ioNodeName: IoName, value: Json)
    extends HiddenNodeDef:

  def toNew[F[_]: MonadThrow](getIoNode: IoName => F[IoNode[F]]): F[ConcreteNode.New] =
    def parseValue(variable: IoVariable[F, ?]): F[IoIndex] = variable match
      case v: BooleanIoVariableLike[F] => MonadThrow[F].fromEither(value.as[Boolean]).flatMap(v.indexForValue)
      case v: FloatIoVariableLike[F]   => MonadThrow[F].fromEither(value.as[Double]).flatMap(v.indexForValue)
      case v: IntIoVariableLike[F]     => MonadThrow[F].fromEither(value.as[Long]).flatMap(v.indexForValue)
      case v: ListStrIoVariableLike[F] => MonadThrow[F].fromEither(value.as[String]).flatMap(v.indexForValue)
      case v                           => s"Unsupported variable type for value: $value, variable: $v".assertionError

    for
      ioNode <- getIoNode(ioNodeName)
      valueIndex <- parseValue(ioNode.variable)
    yield ConcreteNode.New(
      name = Some(name),
      description = description,
      ioNodeName = ioNodeName,
      valueIndex = valueIndex
    )

final case class AbstractNodeDef(name: HnName, description: Option[Description]) extends HiddenNodeDef:
  def toNew: AbstractNode.New = AbstractNode.New(name = Some(name), description)

object HiddenNodeDef:
  import io.circe.generic.auto.*
  import planning.engine.api.model.json.values.*

  given Encoder[HiddenNodeDef] = new Encoder[HiddenNodeDef]:
    final def apply(data: HiddenNodeDef): Json = data match
      case con: ConcreteNodeDef => Json.obj("type" -> Json.fromString("ConcreteNode"), "data" -> con.asJson)
      case abs: AbstractNodeDef => Json.obj("type" -> Json.fromString("AbstractNode"), "data" -> abs.asJson)

  given Decoder[HiddenNodeDef] = new Decoder[HiddenNodeDef]:
    final def apply(c: HCursor): Decoder.Result[HiddenNodeDef] =
      for
        tpe <- c.downField("type").as[String]
        data <- tpe match
          case "ConcreteNode" => c.downField("data").as[ConcreteNodeDef]
          case "AbstractNode" => c.downField("data").as[AbstractNodeDef]
      yield data
