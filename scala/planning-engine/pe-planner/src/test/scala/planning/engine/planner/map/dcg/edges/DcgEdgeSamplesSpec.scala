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
| created: 2026-01-16 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.HnIndex
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.{Indexies, Links, Thens}

class DcgEdgeSamplesSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val indexies1: Map[SampleId, Indexies] = Map(SampleId(10) -> DcgEdgeSamples.Indexies(HnIndex(1), HnIndex(1)))
    lazy val indexies2: Map[SampleId, Indexies] = Map(SampleId(20) -> DcgEdgeSamples.Indexies(HnIndex(2), HnIndex(3)))

  "DcgEdgeSamples.joinIndexies" should:
    "join two index maps without conflicts" in newCase[CaseData]: (tn, data) =>
      Links.empty.joinIndexies[IO](data.indexies1, data.indexies2, "test").logValue(tn)
        .asserting(_ mustBe (data.indexies1 ++ data.indexies2))

    "fail to join two index maps with conflicts" in newCase[CaseData]: (tn, data) =>
      Links.empty.joinIndexies[IO](data.indexies1, data.indexies1, "test").logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate test sample"))

    "fail to join two index maps with source index conflicts" in newCase[CaseData]: (tn, data) =>
      val conflictingIndexies = Map(SampleId(30) -> DcgEdgeSamples.Indexies(HnIndex(1), HnIndex(4)))

      Links.empty
        .joinIndexies[IO](data.indexies1, conflictingIndexies, "test").logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate test source"))

    "fail to join two index maps with target index conflicts" in newCase[CaseData]: (tn, data) =>
      val conflictingIndexies = Map(SampleId(30) -> DcgEdgeSamples.Indexies(HnIndex(5), HnIndex(1)))
      Links.empty
        .joinIndexies[IO](data.indexies1, conflictingIndexies, "test").logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate test target"))

  "DcgEdgeSamples.Links.empty" should:
    "be an empty Links instance" in newCase[CaseData]: (tn, data) =>
      Links.empty.indexies.pure[IO].asserting(_ mustBe empty)

  "DcgEdgeSamples.Links.join" should:
    "join two Links instances without conflicts" in newCase[CaseData]: (tn, data) =>
      Links(data.indexies1).join[IO](Links(data.indexies2)).logValue(tn)
        .asserting(_.indexies mustBe (data.indexies1 ++ data.indexies2))

  "DcgEdgeSamples.Thens.empty" should:
    "be an empty Thens instance" in newCase[CaseData]: (tn, data) =>
      Thens.empty.indexies.pure[IO].asserting(_ mustBe empty)

  "DcgEdgeSamples.Thens.join" should:
    "join two Thens instances without conflicts" in newCase[CaseData]: (tn, data) =>
      Thens(data.indexies1).join[IO](Thens(data.indexies2)).logValue(tn)
        .asserting(_.indexies mustBe (data.indexies1 ++ data.indexies2))
