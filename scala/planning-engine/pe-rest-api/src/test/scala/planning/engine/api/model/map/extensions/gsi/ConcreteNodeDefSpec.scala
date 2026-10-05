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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.effect.IO
import cats.effect.cps.*
import io.circe.Json
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.api.model.map.extensions.gsi.ConcreteNodeDefEx.toNew
import planning.engine.api.model.map.payload.ConcreteNodeDef
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.{InputNode, IoNode}
import planning.engine.map.io.variable.IntIoVariableLike

class ConcreteNodeDefSpec extends UnitSpecWithData with AsyncIdiomaticMockito:

  private class CaseData extends Case:
    lazy val testValue = 1234L
    lazy val testConcreteNodeDef = ConcreteNodeDef(
      HnName("concreteNode"),
      Description.some("testConcreteNodeDef"),
      IoName("ioNode"),
      Json.fromLong(testValue),
    )

  "ConcreteNodeDef.toNew" should:
    "convert to ConcreteNode.New" in newCase[CaseData]: (_, data) =>
      val testIoIndex = IoIndex(321)
      val mockedIntIoVariable = mock[IntIoVariableLike[IO]]
      val ioNode = InputNode(data.testConcreteNodeDef.ioNodeName, mockedIntIoVariable)
      val mockedGetIoNode = mock[IoName => IO[IoNode[IO]]]

      mockedGetIoNode(data.testConcreteNodeDef.ioNodeName) returns IO.pure(ioNode)
      mockedIntIoVariable.indexForValue(data.testValue) returns IO.pure(testIoIndex)

      async[IO]:
        val result = data.testConcreteNodeDef.toNew[IO](mockedGetIoNode).await

        mockedGetIoNode(data.testConcreteNodeDef.ioNodeName) was called
        mockedIntIoVariable.indexForValue(data.testValue) was called
        result mustEqual ConcreteNode.New(
          name = Some(data.testConcreteNodeDef.name),
          description = data.testConcreteNodeDef.description,
          ioNodeName = data.testConcreteNodeDef.ioNodeName,
          valueIndex = testIoIndex,
        )
