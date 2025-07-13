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
| created: 2025-07-07 |||||||||||*/

package planning.engine.map.subgraph

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.graph.MapGraphTestData
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.{SampleData, SampleEdge}

class NextSampleEdgeSpec extends UnitSpecWithData with MapGraphTestData:

  private class CaseData extends Case:
    val sampleDataMap: Map[SampleId, SampleData] = Map(testSampleData.id -> testSampleData)
    val hnMap: Map[HnId, HiddenNode[IO]] = hiddenNodes.map(hn => hn.id -> hn).toMap
    val currentHnId: HnId = hiddenNodes(1).id
    val nextHnId: HnId = hiddenNodes(2).id

    val testEdge = SampleEdge(
      source = SampleEdge.End(currentHnId, HnIndex(10)),
      target = SampleEdge.End(nextHnId, HnIndex(20)),
      edgeType = EdgeType.THEN,
      sampleId = testSampleData.id
    )

  "NextSampleEdge.fromSampleEdge(...)" should:
    "build NextSampleEdge from SampleEdge" in newCase[CaseData]: (tn, data) =>
      NextSampleEdge.fromSampleEdge(data.testEdge, data.sampleDataMap, data.hnMap)
        .logValue(tn, "NextSampleEdge.fromSampleEdge")
        .asserting { nextEdge =>
          nextEdge.sampleData mustEqual data.sampleDataMap(data.testEdge.sampleId)
          nextEdge.currentValue mustEqual data.testEdge.source.value
          nextEdge.edgeType mustEqual data.testEdge.edgeType
          nextEdge.nextValue mustEqual data.testEdge.target.value
          nextEdge.nextHn.id mustEqual data.nextHnId
        }
