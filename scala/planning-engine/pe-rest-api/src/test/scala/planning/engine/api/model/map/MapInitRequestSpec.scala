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
import planning.engine.api.model.map.payload.*
import planning.engine.common.UnitSpecWithData
import planning.engine.map.graph.MapMetadata
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.{BooleanIoVariable, FloatIoVariable, IntIoVariable, ListStrIoVariable}

class MapInitRequestSpec extends UnitSpecWithData with TestApiData:

  private class CaseData extends Case:
    lazy val expectedMetadata = MapMetadata(
      name = testMapInitRequest.name,
      description = testMapInitRequest.description
    )

    lazy val expectedInputNodes = testMapInitRequest.inputNodes.map:
      case BooleanIoNodeDef(name, acceptableValues) => InputNode[IO](name, BooleanIoVariable[IO](acceptableValues))
      case FloatIoNodeDef(name, min, max)           => InputNode[IO](name, FloatIoVariable[IO](min, max))
      case n                                        => fail(s"Unexpected node definition: $n")

    lazy val expectedOutputNodes = testMapInitRequest.outputNodes.map:
      case IntIoNodeDef(name, min, max)     => OutputNode[IO](name, IntIoVariable[IO](min, max))
      case ListStrIoNodeDef(name, elements) => OutputNode[IO](name, ListStrIoVariable[IO](elements))
      case n                                => fail(s"Unexpected node definition: $n")

  "MapInitRequest.toMetadata" should:
    "convert valid request to metadata" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.toMetadata[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedMetadata)

  "MapInitRequest.toInputNodes" should:
    "convert valid input nodes to InputNode instances" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.toInputNodes[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedInputNodes)

  "MapInitRequest.toOutputNodes" should:
    "convert valid output nodes to OutputNode instances" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.toOutputNodes[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedOutputNodes)
