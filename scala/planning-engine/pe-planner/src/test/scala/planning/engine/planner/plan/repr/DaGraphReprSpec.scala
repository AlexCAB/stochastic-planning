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
| created: 2026-03-23 |||||||||||*/

package planning.engine.planner.plan.repr

import cats.effect.IO
import cats.effect.cps.*
import cats.effect.cps.async
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.plan.dag.ChainDagTestData

class DaGraphReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with ChainDagTestData

  "DaGraphRepr.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val strRepr = data.chainDaGraph.repr.await
        logInfo(tn, s"DaGraph.repr:\n$strRepr").await

        strRepr must include("DaGraph")
