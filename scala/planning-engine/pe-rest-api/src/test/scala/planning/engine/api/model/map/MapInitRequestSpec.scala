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
import planning.engine.common.values.text.{Name, Description}
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, FloatIoVariable, IntIoVariable, ListStrIoVariable}
import planning.engine.map.knowledge.graph.Metadata

class MapInitRequestSpec extends UnitSpecIO:

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

    lazy val expectedMetadata = Metadata(
      name = Name.fromStringOptional("ValidMap"),
      description = Description.fromStringOptional("A valid map description")
    )

    lazy val expectedInputNodes = List(
      InputNode[IO](Name("inputBoolean"), BooleanIoVariable(Set(true, false))),
      InputNode[IO](Name("inputFloat"), FloatIoVariable(0.0f, 1.0f))
    ).sequence.unsafeRunSync()

    lazy val expectedOutputNodes = List(
      OutputNode[IO](Name("outputInt"), IntIoVariable(0, 10)),
      OutputNode[IO](Name("outputListStr"), ListStrIoVariable(List("a", "b", "c")))
    ).sequence.unsafeRunSync()

  "toMetadata" should:
    "convert valid request to metadata" in newCase[CaseData]: data =>
      data.validRequest.toMetadata[IO]
        .logValue
        .asserting(_ mustEqual data.expectedMetadata)

  "toInputNodes" should:
    "convert valid input nodes to InputNode instances" in newCase[CaseData]: data =>
      data.validRequest.toInputNodes[IO]
        .logValue
        .asserting(_ mustEqual data.expectedInputNodes)

  "toOutputNodes" should:
    "convert valid output nodes to OutputNode instances" in newCase[CaseData]: data =>
      data.validRequest.toOutputNodes[IO]
        .logValue
        .asserting(_ mustEqual data.expectedOutputNodes)
