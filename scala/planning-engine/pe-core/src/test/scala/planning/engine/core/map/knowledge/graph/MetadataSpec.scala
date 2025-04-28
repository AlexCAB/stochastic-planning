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
| created: 2025-04-10 |||||||||||*/

package planning.engine.core.map.knowledge.graph

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import neotypes.model.types.Value
import planning.engine.common.values.{OpDescription, OpName}

class MetadataSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val metadataProps = Map(
      "name" -> Value.Str("TestName"),
      "description" -> Value.Str("TestDescription")
    )

    val metadata = Metadata(OpName(Some("TestName")), OpDescription(Some("TestDescription")))

  "toProperties" should:
    "return correct properties map for metadata" in newCase[CaseData]: data =>
      data.metadata.toProperties[IO]
        .logValue
        .asserting(_ mustEqual data.metadataProps)

  "fromProperties" should:
    "create metadata from properties" in newCase[CaseData]: data =>
      Metadata.fromProperties[IO](data.metadataProps)
        .logValue
        .asserting(_ mustEqual data.metadata)
