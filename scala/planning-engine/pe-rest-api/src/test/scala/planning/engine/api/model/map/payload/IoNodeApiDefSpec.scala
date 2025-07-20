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
| created: 2025-04-28 |||||||||||*/

package planning.engine.api.model.map.payload

import cats.effect.IO
import cats.effect.cps.*
import io.circe.syntax.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.Name

class IoNodeApiDefSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val booleanIoNode: IoNodeApiDef = BooleanIoNodeDef(Name("boolDef"), Set(true, false))
    lazy val floatIoNode: IoNodeApiDef = FloatIoNodeDef(Name("floatDef"), min = -1, max = 1)
    lazy val intIoNode: IoNodeApiDef = IntIoNodeDef(Name("intDef"), min = 0, max = 10)
    lazy val listStrIoNode: IoNodeApiDef = ListStrIoNodeDef(Name("listStrDef"), elements = List("a", "b", "c"))

  "IoNodeApiDef" should:
    "decode and encode BooleanIoNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val encoded = data.booleanIoNode.asJson
        logInfo(tn, s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        logInfo(tn, s"Decoded value: $decoded").await

        decoded mustEqual data.booleanIoNode

    "decode and encode FloatIoNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val encoded = data.floatIoNode.asJson
        logInfo(tn, s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        logInfo(tn, s"Decoded value: $decoded").await

        decoded mustEqual data.floatIoNode

    "decode and encode IntIoNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val encoded = data.intIoNode.asJson
        logInfo(tn, s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        logInfo(tn, s"Decoded value: $decoded").await

        decoded mustEqual data.intIoNode

    "decode and encode ListStrIoNode" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val encoded = data.listStrIoNode.asJson
        logInfo(tn, s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        logInfo(tn, s"Decoded value: $decoded").await

        decoded mustEqual data.listStrIoNode
