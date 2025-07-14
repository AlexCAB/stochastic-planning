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
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.IoIndex
import planning.engine.map.hidden.node.{ConcreteNode, AbstractNode}
import cats.effect.cps.*
import io.circe.syntax.*

class HiddenNodeDefSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val testConcreteNodeDef = ConcreteNodeDef(Name("concreteNode"), Name("ioNode"), IoIndex(0))
    lazy val testAbstractNodeDef = AbstractNodeDef(Name("abstractNode"))

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

  "ConcreteNodeDef.toNew" should:
    "convert to ConcreteNode.New" in newCase[CaseData]: (_, data) =>
      async[IO]:
        data.testConcreteNodeDef.toNew mustEqual ConcreteNode.New(
          name = Some(data.testConcreteNodeDef.name),
          ioNodeName = data.testConcreteNodeDef.ioNodeName,
          valueIndex = data.testConcreteNodeDef.valueIndex
        )

  "AbstractNodeDef.toNew" should:
    "convert to AbstractNode.New" in newCase[CaseData]: (_, data) =>
      async[IO]:
        data.testAbstractNodeDef.toNew mustEqual AbstractNode.New(
          name = Some(data.testAbstractNodeDef.name)
        )
