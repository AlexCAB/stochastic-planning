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
| created: 2026-02-02 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.map.test.data.DcgEdgeTestData

class DcgEdgeReprSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgEdgeTestData:
    def expectedReprTarget(arrow: String): String = s"| -[$arrow]-> ${trgAbs.reprNode}"

  "DcgEdgeRepr.repr" should:
    "return correct string representation for link only edge" in newCase[CaseData]: (tn, data) =>
      data.dcgEdgeLink.repr.pure[IO].logValue(tn).asserting(_ mustBe data.expectedReprTarget("L"))

    "return correct string representation for then only edge" in newCase[CaseData]: (tn, data) =>
      data.dcgEdgeThen.repr.pure[IO].logValue(tn).asserting(_ mustBe data.expectedReprTarget("T"))
