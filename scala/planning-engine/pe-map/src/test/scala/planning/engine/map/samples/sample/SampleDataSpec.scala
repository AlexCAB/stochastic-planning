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
    val sampleNewData = Sample.New(
      probabilityCount = 100,
      utility = 0.75,
      name = Name.some("Sample Node"),
      description = Description.some("This is a sample node for testing."),
      edges = Set.empty
    )

    val sampleData = SampleData(
      id = SampleId(1),
      probabilityCount = sampleNewData.probabilityCount,
      utility = sampleNewData.utility,
      name = sampleNewData.name,
      description = sampleNewData.description
    )

    lazy val nodeValues = Map(
      PROP.SAMPLE_ID -> Value.Integer(sampleData.id.value),
      PROP.PROBABILITY_COUNT -> Value.Integer(sampleData.probabilityCount),
      PROP.UTILITY -> Value.Decimal(sampleData.utility),
      PROP.NAME -> Value.Str(sampleData.name.get.value),
      PROP.DESCRIPTION -> Value.Str(sampleData.description.get.value)
    )

    lazy val rawNode = Node("1", Set(SAMPLE_LABEL), nodeValues)

  "SampleData.fromNode(...)" should:
    "build SampleData from DB Node" in newCase[CaseData]: (tn, data) =>
      SampleData.fromNode[IO](data.rawNode).logValue(tn, "fromNode")
        .asserting(_ mustEqual data.sampleData)

  "SampleData.fromNew(...)" should:
    "build SampleData from Sample.New data" in newCase[CaseData]: (tn, data) =>
      SampleData.fromNew[IO](data.sampleData.id, data.sampleNewData).logValue(tn, "fromNew")
        .asserting(_ mustEqual data.sampleData)
