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
| created: 2025-07-14 |||||||||||*/

package planning.engine.api.model.map.payload

import cats.effect.IO
import cats.effect.cps.*
import io.circe.Json
import io.circe.syntax.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description

class HiddenNodeDefSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val testConcreteNodeDef = ConcreteNodeDef(
      HnName("concreteNode"),
      Description.some("testConcreteNodeDef"),
      IoName("ioNode"),
      Json.fromLong(1234L),
    )

    lazy val testAbstractNodeDef = AbstractNodeDef(HnName("abstractNode"), Description.some("testAbstractNodeDef"))

  "HiddenNodeDef" should:
    "decode and encode ConcreteNodeDef" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val encoded = (data.testConcreteNodeDef: HiddenNodeDef).asJson
        logInfo(tn, s"Encoded ConcreteNodeDef JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[HiddenNodeDef]).await
        logInfo(tn, s"Decoded ConcreteNodeDef value: $decoded").await

        decoded mustEqual data.testConcreteNodeDef

    "decode and encode AbstractNodeDef" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val encoded = (data.testAbstractNodeDef: HiddenNodeDef).asJson
        logInfo(tn, s"Encoded AbstractNodeDef JSON: $encoded").await

        val decoded = IO.fromEither(encoded.as[HiddenNodeDef]).await
        logInfo(tn, s"Decoded AbstractNodeDef value: $decoded").await

        decoded mustEqual data.testAbstractNodeDef
