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
import planning.engine.common.values.text.Description
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import cats.effect.cps.*
import io.circe.Json
import io.circe.syntax.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.HnName
import planning.engine.map.io.node.{InputNode, IoNode}
import planning.engine.map.io.variable.IntIoVariableLike

class HiddenNodeDefSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case:
    lazy val testValue = 1234L
    lazy val testConcreteNodeDef = ConcreteNodeDef(
      HnName("concreteNode"),
      Description.some("testConcreteNodeDef"),
      IoName("ioNode"),
      Json.fromLong(testValue)
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

  "ConcreteNodeDef.toNew" should:
    "convert to ConcreteNode.New" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val testIoIndex = IoIndex(321)
        val mockedIntIoVariable = mock[IntIoVariableLike[IO]]
        val ioNode = InputNode(data.testConcreteNodeDef.ioNodeName, mockedIntIoVariable)
        val mockedGetIoNode = mock[IoName => IO[IoNode[IO]]]

        mockedGetIoNode.apply.expects(data.testConcreteNodeDef.ioNodeName).returning(IO.pure(ioNode)).once()
        mockedIntIoVariable.indexForValue.expects(data.testValue).returning(IO.pure(testIoIndex)).once()

        data.testConcreteNodeDef.toNew[IO](mockedGetIoNode).await mustEqual ConcreteNode.New(
          name = Some(data.testConcreteNodeDef.name),
          description = data.testConcreteNodeDef.description,
          ioNodeName = data.testConcreteNodeDef.ioNodeName,
          valueIndex = testIoIndex
        )

  "AbstractNodeDef.toNew" should:
    "convert to AbstractNode.New" in newCase[CaseData]: (_, data) =>
      async[IO]:
        data.testAbstractNodeDef.toNew mustEqual AbstractNode.New(
          name = Some(data.testAbstractNodeDef.name),
          description = data.testAbstractNodeDef.description
        )
