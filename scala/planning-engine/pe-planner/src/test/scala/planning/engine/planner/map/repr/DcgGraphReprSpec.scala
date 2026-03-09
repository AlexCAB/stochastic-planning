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

package planning.engine.planner.map.repr

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.test.data.{DcgGraphTestData, DcgSampleReprTestData}

class DcgGraphReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgGraphTestData with DcgSampleReprTestData:
    lazy val notConnected = Set(MnId.Con(998), MnId.Abs(999))

    lazy val dcgDcgGraph = emptyDcgGraph
      .makeAndAddTestNodes(allMnIds ++ notConnected)
      .addTestDcgSample(complexDcgSample)

  "DcgGraphRepr.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val strRepr = data.dcgDcgGraph.repr.await
        logInfo(tn, s"DcgSample.repr:\n$strRepr").await

        strRepr must include("DcgGraph")
