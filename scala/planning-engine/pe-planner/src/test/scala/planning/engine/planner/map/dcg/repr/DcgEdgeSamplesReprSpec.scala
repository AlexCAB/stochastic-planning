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
import planning.engine.common.values.node.HnIndex
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.edges.DcgSamples
import planning.engine.planner.map.dcg.edges.DcgSamples.{Indexies, Links, Thens}

class DcgEdgeSamplesReprSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val indexies: Map[SampleId, Indexies] = Map(SampleId(10) -> DcgSamples.Indexies(HnIndex(1), HnIndex(1)))
    lazy val links1: Links = Links(indexies)
    lazy val thens1: Thens = Thens(indexies)

  "DcgEdgeSamplesReprSpec.repr" should:
    "return correct short string representation for Link" in newCase[CaseData]: (tn, data) =>
      data.links1.repr.pure[IO].logValue(tn).asserting(_ mustBe "L")

    "return correct short string representation for Then" in newCase[CaseData]: (tn, data) =>
      data.thens1.repr.pure[IO].logValue(tn).asserting(_ mustBe "T")

    "return correct short string representation for empty" in newCase[CaseData]: (tn, data) =>
      Links.empty.repr.pure[IO].logValue(tn).asserting(_ mustBe "_")
