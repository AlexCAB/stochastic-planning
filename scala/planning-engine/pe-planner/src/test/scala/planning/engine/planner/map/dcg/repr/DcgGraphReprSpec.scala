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
| created: 2026-03-03 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.map.test.data.{DcgGraphTestData, DcgSampleReprTestData}

class DcgGraphReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgGraphTestData with DcgSampleReprTestData:
    
    lazy val dcgDcgGraph = emptyDcgGraph
      .makeAndAddNodesFromIds(allMnIds)
      .addTestDcgSample(complexDcgSample)

  "DcgGraphRepr.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val strRepr = data.dcgDcgGraph.repr.await
        logInfo(tn, s"DcgSample.repr:\n$strRepr").await

        strRepr must include("DcgGraph")
