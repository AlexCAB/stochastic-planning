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

import planning.engine.common.enums.EdgeType
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}

package object enums:

  implicit val encodeEdgeType: Encoder[EdgeType] = new Encoder[EdgeType]:
    final def apply(a: EdgeType): Json = Json.fromString(a.toLabel)

  implicit val decodeEdgeType: Decoder[EdgeType] = new Decoder[EdgeType]:
    final def apply(c: HCursor): Decoder.Result[EdgeType] = c.as[String].flatMap:
      case str if str.nonEmpty => EdgeType.fromLabel(str).left.map(DecodingFailure(_, c.history))
      case _                   => Left(DecodingFailure("EdgeType cannot be empty", c.history))
