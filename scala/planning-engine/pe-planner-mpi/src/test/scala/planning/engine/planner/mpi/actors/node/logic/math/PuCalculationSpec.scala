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
| created: 2026-10-16 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic.math

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.IoTime
import planning.engine.planner.mpi.actors.node.data.Message.ActivationCon

class PuCalculationSpec extends UnitSpecWithData:
  private object TestPuCalculation extends PuCalculation

  private class CaseData extends Case:
    val time: IoTime = IoTime(1L)
    val expectedPu: PU = PU(p = 1.0, u = 0.0)

  "PuCalculation.calcInitPu(...)" should:
    "return probability 1.0 and zero utility for activation with utility" in newCase[CaseData]: (tn, data) =>
      import data.*
      TestPuCalculation.calcInitPu[IO](ActivationCon(time, Some(0.5))).logValue(tn).asserting(_ mustBe expectedPu)

    "return probability 1.0 and zero utility for activation without utility" in newCase[CaseData]: (tn, data) =>
      import data.*
      TestPuCalculation.calcInitPu[IO](ActivationCon(time, None)).logValue(tn).asserting(_ mustBe expectedPu)
