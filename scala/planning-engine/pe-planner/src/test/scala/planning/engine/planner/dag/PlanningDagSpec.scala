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
| created: 2025-08-26 |||||||||||*/

package planning.engine.planner.dag

import cats.effect.IO
import cats.effect.cps.async
import planning.engine.common.UnitSpecWithData

import cats.syntax.all.*
import cats.effect.cps.*

class PlanningDagSpec extends UnitSpecWithData with DagTestData:

  private class CaseData extends Case:
    lazy val planningDag = PlanningDag[IO]().unsafeRunSync()

  "PlanningDag.modifyContextBoundary(...)" should:
    "modify context boundary" in newCase[CaseData]: (_, data) =>
      async[IO]:
        data.planningDag.getContextBoundary.await mustEqual Set()
        data.planningDag.modifyContextBoundary(con => (con + absStateNode1, 1234).pure).await mustEqual 1234
        data.planningDag.getContextBoundary.await mustEqual Set(absStateNode1)
        data.planningDag.modifyContextBoundary(con => (con + conStateNode1, 4321).pure).await mustEqual 4321
        data.planningDag.getContextBoundary.await mustEqual Set(absStateNode1, conStateNode1)
