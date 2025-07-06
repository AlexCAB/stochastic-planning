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
| created: 2025-07-06 |||||||||||*/

package planning.engine.map.samples.sample

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.db.Neo4j.SAMPLE_LABEL
import neotypes.model.types.Node
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.properties.*
import neotypes.model.types.Value

class SampleDataSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val expectedSampleData = SampleData(
      id = SampleId(1),
      probabilityCount = 100,
      utility = 0.75,
      name = Some(Name("Sample Node")),
      description = Some(Description("This is a sample node for testing."))
    )

    lazy val nodeValues = Map(
      PROP.SAMPLE_ID -> Value.Integer(expectedSampleData.id.value),
      PROP.PROBABILITY_COUNT -> Value.Integer(expectedSampleData.probabilityCount),
      PROP.UTILITY -> Value.Decimal(expectedSampleData.utility),
      PROP.NAME -> Value.Str(expectedSampleData.name.get.value),
      PROP.DESCRIPTION -> Value.Str(expectedSampleData.description.get.value)
    )

    lazy val rawNode = Node("1", Set(SAMPLE_LABEL), nodeValues)

  "SampleData.fromNode(...)" should:
    "build SampleData from DB Node" in newCase[CaseData]: (tn, data) =>
      SampleData.fromNode[IO](data.rawNode).logValue(tn, "fromNode")
        .asserting(_ mustEqual data.expectedSampleData)
