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

package planning.engine.api.model

import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import planning.engine.common.values.text.{Description, Name}
import cats.syntax.all.*
import planning.engine.common.values.node.IoIndex
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

  implicit val encodeDescription: Encoder[Description] = encodeStringVal[Description]
  implicit val decodeDescription: Decoder[Description] = decodeStringVal[Description](Description.apply)

  implicit val encodeSampleId: Encoder[SampleId] = encodeLongVal[SampleId]
  implicit val decodeSampleId: Decoder[SampleId] = decodeLongVal[SampleId](SampleId.apply)

  implicit val encodeIoIndex: Encoder[IoIndex] = encodeLongVal[IoIndex]
  implicit val decodeIoIndex: Decoder[IoIndex] = decodeLongVal[IoIndex](IoIndex.apply)
