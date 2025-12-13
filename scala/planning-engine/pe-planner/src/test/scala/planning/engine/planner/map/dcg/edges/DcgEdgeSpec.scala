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
import planning.engine.planner.map.MapTestData

class DcgEdgeSpec extends UnitSpecWithData with MapTestData:

  private class CaseData extends Case:
    lazy val hiddenEdge: HiddenEdge = testHiddenEdge

  "DcgEdge.apply(...)" should:
    "crete DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val dcgEdge = DcgEdge[IO](data.hiddenEdge).await
        logInfo(n, s"dcgEdge: $dcgEdge").await

        dcgEdge.key.edgeType mustBe data.hiddenEdge.edgeType
        dcgEdge.key.sourceId mustBe data.hiddenEdge.sourceId
        dcgEdge.key.targetId mustBe data.hiddenEdge.targetId
        dcgEdge.samples.size mustBe data.hiddenEdge.samples.size

        dcgEdge.samples mustBe data.hiddenEdge.samples.map(s => s.sampleId -> s).toMap
