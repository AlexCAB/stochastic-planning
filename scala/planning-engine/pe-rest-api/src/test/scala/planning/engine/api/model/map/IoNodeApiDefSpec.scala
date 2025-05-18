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

package planning.engine.api.model.map

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import org.typelevel.log4cats.Logger

import cats.effect.cps.*
import io.circe.syntax.*

class IoNodeApiDefSpec extends UnitSpecIO:

  private class CaseData extends Case:
    lazy val booleanIoNode: IoNodeApiDef = BooleanIoNode("boolDef", Set(true, false))
    lazy val floatIoNode: IoNodeApiDef = FloatIoNode("floatDef", min = -1, max = 1)
    lazy val intIoNode: IoNodeApiDef = IntIoNode("intDef", min = 0, max = 10)
    lazy val listStrIoNode: IoNodeApiDef = ListStrIoNode("listStrDef", elements = List("a", "b", "c"))

  "IoNodeApiDef" should:
    "decode and encode BooleanIoNode" in newCase[CaseData]: data =>
      async[IO]:
        val encoded = data.booleanIoNode.asJson
        Logger[IO].info(s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        Logger[IO].info(s"Decoded value: $decoded").await

        decoded mustEqual data.booleanIoNode

    "decode and encode FloatIoNode" in newCase[CaseData]: data =>
      async[IO]:
        val encoded = data.floatIoNode.asJson
        Logger[IO].info(s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        Logger[IO].info(s"Decoded value: $decoded").await

        decoded mustEqual data.floatIoNode

    "decode and encode IntIoNode" in newCase[CaseData]: data =>
      async[IO]:
        val encoded = data.intIoNode.asJson
        Logger[IO].info(s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        Logger[IO].info(s"Decoded value: $decoded").await

        decoded mustEqual data.intIoNode

    "decode and encode ListStrIoNode" in newCase[CaseData]: data =>
      async[IO]:
        val encoded = data.listStrIoNode.asJson
        Logger[IO].info(s"Encoded JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[IoNodeApiDef]).await
        Logger[IO].info(s"Decoded value: $decoded").await

        decoded mustEqual data.listStrIoNode
