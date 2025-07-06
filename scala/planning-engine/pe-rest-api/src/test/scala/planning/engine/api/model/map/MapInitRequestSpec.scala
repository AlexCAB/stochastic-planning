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
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.{Name, Description}
import planning.engine.map.graph.MapMetadata
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, FloatIoVariable, IntIoVariable, ListStrIoVariable}

class MapInitRequestSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val validRequest = MapInitRequest(
      name = Some("ValidMap"),
      description = Some("A valid map description"),
      inputNodes = List(
        BooleanIoNode("inputBoolean", Set(true, false)),
        FloatIoNode("inputFloat", 0.0f, 1.0f)
      ),
      outputNodes = List(
        IntIoNode("outputInt", 0, 10),
        ListStrIoNode("outputListStr", List("a", "b", "c"))
      )
    )

    lazy val expectedMetadata = MapMetadata(
      name = Some(Name("ValidMap")),
      description = Some(Description("A valid map description"))
    )

    lazy val expectedInputNodes = List(
      InputNode[IO](Name("inputBoolean"), BooleanIoVariable[IO](Set(true, false))),
      InputNode[IO](Name("inputFloat"), FloatIoVariable[IO](0.0f, 1.0f))
    )

    lazy val expectedOutputNodes = List(
      OutputNode[IO](Name("outputInt"), IntIoVariable(0, 10)),
      OutputNode[IO](Name("outputListStr"), ListStrIoVariable(List("a", "b", "c")))
    )

  "MapInitRequest.toMetadata" should:
    "convert valid request to metadata" in newCase[CaseData]: (tn, data) =>
      data.validRequest.toMetadata[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedMetadata)

  "MapInitRequest.toInputNodes" should:
    "convert valid input nodes to InputNode instances" in newCase[CaseData]: (tn, data) =>
      data.validRequest.toInputNodes[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedInputNodes)

  "MapInitRequest.toOutputNodes" should:
    "convert valid output nodes to OutputNode instances" in newCase[CaseData]: (tn, data) =>
      data.validRequest.toOutputNodes[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedOutputNodes)
