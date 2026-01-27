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

package planning.engine.api.model.json

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import planning.engine.common.values.text.{Description, Name}
import cats.syntax.all.*
import planning.engine.api.model.map.payload.HiddenNodeDef
import planning.engine.common.values.db.DbName
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{AbsId, ConId, HnId, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.{LongVal, StringVal}

package object values:

  private def encodeStringVal[V <: StringVal]: Encoder[V] = new Encoder[V]:
    final def apply(a: V): Json = Json.fromString(a.value)

  private def decodeStringVal[V <: StringVal](make: String => V): Decoder[V] = new Decoder[V]:
    final def apply(c: HCursor): Decoder.Result[V] = c.as[String].flatMap:
      case str if str.nonEmpty => make(str).asRight
      case _                   => Left(DecodingFailure("StringVal cannot be empty", c.history))

  private def encodeLongVal[V <: LongVal]: Encoder[V] = new Encoder[V]:
    final def apply(a: V): Json = Json.fromLong(a.value)

  private def decodeLongVal[V <: LongVal](make: Long => V): Decoder[V] = new Decoder[V]:
    final def apply(c: HCursor): Decoder.Result[V] = c.as[Long].map(make)

  implicit val encodeName: Encoder[Name] = encodeStringVal[Name]
  implicit val decodeName: Decoder[Name] = decodeStringVal[Name](Name.apply)

  implicit val encodeIoName: Encoder[IoName] = encodeStringVal[IoName]
  implicit val decodeIoName: Decoder[IoName] = decodeStringVal[IoName](IoName.apply)

  implicit val encodeHnName: Encoder[HnName] = encodeStringVal[HnName]
  implicit val decodeHnName: Decoder[HnName] = decodeStringVal[HnName](HnName.apply)

  implicit val encodeDescription: Encoder[Description] = encodeStringVal[Description]
  implicit val decodeDescription: Decoder[Description] = decodeStringVal[Description](Description.apply)

  implicit val encodeDbName: Encoder[DbName] = encodeStringVal[DbName]
  implicit val decodeDbName: Decoder[DbName] = decodeStringVal[DbName](DbName.apply)

  implicit val encodeSampleId: Encoder[SampleId] = encodeLongVal[SampleId]
  implicit val decodeSampleId: Decoder[SampleId] = decodeLongVal[SampleId](SampleId.apply)

  implicit val encodeIoIndex: Encoder[IoIndex] = encodeLongVal[IoIndex]
  implicit val decodeIoIndex: Decoder[IoIndex] = decodeLongVal[IoIndex](IoIndex.apply)

  given Encoder[HnId] = new Encoder[HnId]:
    final def apply(data: HnId): Json = data match
      case con: ConId => Json.obj("type" -> Json.fromString("ConId"), "data" -> Json.fromLong(con.value))
      case abs: AbsId => Json.obj("type" -> Json.fromString("AbsId"), "data" -> Json.fromLong(abs.value))

  given Decoder[HnId] = new Decoder[HnId]:
    final def apply(c: HCursor): Decoder.Result[HnId] =
      for
        tpe <- c.downField("type").as[String]
        data <- tpe match
          case "ConId" => c.downField("data").as[Long].map(ConId.apply)
          case "AbsId" => c.downField("data").as[Long].map(AbsId.apply)
      yield data
