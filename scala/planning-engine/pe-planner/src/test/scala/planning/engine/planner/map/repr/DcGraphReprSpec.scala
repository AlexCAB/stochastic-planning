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
import planning.engine.planner.map.test.data.{DcGraphTestData, DcgSampleReprTestData}

class DcGraphReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcGraphTestData with DcgSampleReprTestData:
    lazy val notConnected = Set(MnId.Con(998), MnId.Abs(999))

    lazy val dcDcGraph = emptyDcGraph
      .makeAndAddTestNodes(allMnIds ++ notConnected)
      .addTestDcgSample(complexDcgSample)

  "DcGraphRepr.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val strRepr = data.dcDcGraph.repr.await
        logInfo(tn, s"DcGraph.repr:\n$strRepr").await

        strRepr must include("DcGraph")
