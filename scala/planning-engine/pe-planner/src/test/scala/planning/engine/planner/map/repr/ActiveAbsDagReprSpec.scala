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
| created: 2026-03-08 |||||||||||*/

package planning.engine.planner.map.repr

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.map.test.data.ActiveAbsDagTestData

class ActiveAbsDagReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with ActiveAbsDagTestData

  "ActiveAbsDag.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val strRepr = data.activeAbsDag.repr.await
        logInfo(tn, s"ActiveAbsDag.repr:\n$strRepr").await

        strRepr must include("ActiveAbsDag")
