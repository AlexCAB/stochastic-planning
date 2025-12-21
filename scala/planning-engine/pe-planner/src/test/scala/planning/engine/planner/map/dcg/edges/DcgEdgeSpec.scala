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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.planner.map.MapTestData

class DcgEdgeSpec extends UnitSpecWithData with MapTestData:

  private class CaseData extends Case:
    lazy val hiddenEdge: HiddenEdge = testHiddenEdge
    lazy val sampleEdge: SampleEdge = testSampleEdge

  "DcgEdge.apply(HiddenEdge)" should:
    "crete DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val dcgEdge = DcgEdge[IO](data.hiddenEdge).await
        logInfo(n, s"dcgEdge: $dcgEdge").await

        dcgEdge.key.edgeType mustBe data.hiddenEdge.edgeType
        dcgEdge.key.sourceId mustBe data.hiddenEdge.sourceId
        dcgEdge.key.targetId mustBe data.hiddenEdge.targetId
        dcgEdge.samples.size mustBe data.hiddenEdge.samples.size

        dcgEdge.samples mustBe data.hiddenEdge.samples.map(s => s.sampleId -> s).toMap

  "DcgEdge.apply(SampleEdge)" should:
    "crete DcgEdge correctly from SampleEdge" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val dcgEdge = DcgEdge[IO](data.sampleEdge).await
        logInfo(n, s"dcgEdge: $dcgEdge").await

        dcgEdge.key.edgeType mustBe data.sampleEdge.edgeType
        dcgEdge.key.sourceId mustBe data.sampleEdge.source.hnId
        dcgEdge.key.targetId mustBe data.sampleEdge.target.hnId
        dcgEdge.samples.size mustBe 1

        dcgEdge.samples mustBe Map(data.sampleEdge.sampleId -> DcgEdge.Indexies(
          sourceIndex = data.sampleEdge.source.value,
          targetIndex = data.sampleEdge.target.value
        ))
