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
import planning.engine.common.values.edge.EdgeKey
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.planner.map.dcg.edges.DcgSamples.{Indexies, Links, Thens}

class DcgEdgeDataReprSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val indexies: Map[SampleId, Indexies] = Map(SampleId(10) -> Indexies(HnIndex(1), HnIndex(1)))

    lazy val dcgBothEdge: DcgEdgeData = DcgEdgeData(
      ends = EdgeKey(HnId(1), HnId(2)),
      links = Links(indexies),
      thens = Thens(indexies)
    )

    lazy val dcgLinkEdge: DcgEdgeData = dcgBothEdge.copy(thens = Thens.empty)
    lazy val dcgThenEdge: DcgEdgeData = dcgBothEdge.copy(links = Links.empty)

    def expectedRepr(arrow: String): String =
      s"(${dcgBothEdge.ends.src.vStr}) -[$arrow]-> (${dcgBothEdge.ends.trg.vStr})"

    def expectedReprTarget(arrow: String): String = s"| -[$arrow]-> (${dcgBothEdge.ends.trg.vStr})"

  "DcgEdgeDataRepr.repr(...)" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      data.dcgBothEdge.repr.pure[IO].logValue(tn).asserting(_ mustBe data.expectedRepr("LT"))

    "return correct string representation for link only edge" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.repr.pure[IO].logValue(tn).asserting(_ mustBe data.expectedRepr("L_"))

    "return correct string representation for then only edge" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.repr.pure[IO].logValue(tn).asserting(_ mustBe data.expectedRepr("_T"))

  "DcgEdgeDataRepr.reprTarget(...)" should:
    "return correct string representation for target" in newCase[CaseData]: (tn, data) =>
      data.dcgBothEdge.reprTarget.pure[IO].logValue(tn).asserting(_ mustBe data.expectedReprTarget("LT"))

    "return correct string representation for target for link only edge" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.reprTarget.pure[IO].logValue(tn).asserting(_ mustBe data.expectedReprTarget("L_"))

    "return correct string representation for target for then only edge" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.reprTarget.pure[IO].logValue(tn).asserting(_ mustBe data.expectedReprTarget("_T"))
