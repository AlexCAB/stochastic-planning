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

    lazy val links1: Links = Links(indexies1)
    lazy val thens1: Thens = Thens(indexies2)

  "DcgEdgeSamples.srcHnIndex" should:
    "return source HnIndex set" in newCase[CaseData]: (tn, data) =>
      data.links1.srcHnIndex.pure[IO].asserting(_ mustBe Set(HnIndex(1)))

  "DcgEdgeSamples.trgHnIndex" should:
    "return target HnIndex set" in newCase[CaseData]: (tn, data) =>
      data.links1.trgHnIndex.pure[IO].asserting(_ mustBe Set(HnIndex(1)))

  "DcgEdgeSamples.joinIndexies(...)" should:
    "join two index maps without conflicts" in newCase[CaseData]: (tn, data) =>
      data.links1.joinIndexies[IO](data.indexies2).logValue(tn)
        .asserting(_ mustBe (data.indexies1 ++ data.indexies2))

    "fail to join two index maps with conflicts" in newCase[CaseData]: (tn, data) =>
      data.links1.joinIndexies[IO](data.indexies1).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate links sample"))

    "fail to join two index maps with source index conflicts" in newCase[CaseData]: (tn, data) =>
      val conflictingIndexies = Map(SampleId(30) -> DcgEdgeSamples.Indexies(HnIndex(1), HnIndex(4)))

      data.links1
        .joinIndexies[IO](conflictingIndexies).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate links source"))

    "fail to join two index maps with target index conflicts" in newCase[CaseData]: (tn, data) =>
      val conflictingIndexies = Map(SampleId(30) -> DcgEdgeSamples.Indexies(HnIndex(5), HnIndex(1)))
      data.links1
        .joinIndexies[IO](conflictingIndexies).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate links target"))

  "DcgEdgeSamples.addToMap(...)" should:
    "add new indexies to map without conflicts" in newCase[CaseData]: (tn, data) =>
      data.links1
        .addToMap[IO](SampleId(30), HnIndex(5), HnIndex(6)).logValue(tn)
        .asserting(_ mustBe (data.indexies1 + (SampleId(30) -> Indexies(HnIndex(5), HnIndex(6)))))

    "fail to add indexies with duplicate sample id" in newCase[CaseData]: (tn, data) =>
      data.links1
        .addToMap[IO](SampleId(10), HnIndex(5), HnIndex(6)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate links sample"))

    "fail to add indexies with duplicate source index" in newCase[CaseData]: (tn, data) =>
      data.links1
        .addToMap[IO](SampleId(30), HnIndex(1), HnIndex(6)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(
          "Map edge can't have duplicate links source index"
        ))

    "fail to add indexies with duplicate target index" in newCase[CaseData]: (tn, data) =>
      data.links1
        .addToMap[IO](SampleId(30), HnIndex(5), HnIndex(1)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have dup links target index"))

  "DcgEdgeSamples.size" should:
    "return correct size of indexies map" in newCase[CaseData]: (tn, data) =>
      data.links1.size.pure[IO].asserting(_ mustBe data.indexies1.size)
  
  "DcgEdgeSamples.repr" should:
    "return correct string representation" in newCase[CaseData]: (tn, data) =>
      data.links1.repr.pure[IO].logValue(tn).asserting: repr =>
        repr  must include ("10 | 1 -> 1")

  "DcgEdgeSamples.reprShort" should:
    "return correct short string representation for Link" in newCase[CaseData]: (tn, data) =>
      data.links1.reprShort.pure[IO].logValue(tn).asserting(_ mustBe "L")

    "return correct short string representation for Then" in newCase[CaseData]: (tn, data) =>
      data.thens1.reprShort.pure[IO].logValue(tn).asserting(_ mustBe "T")

    "return correct short string representation for empty" in newCase[CaseData]: (tn, data) =>
      Links.empty.reprShort.pure[IO].logValue(tn).asserting(_ mustBe "_")

  "DcgEdgeSamples.Links.empty" should:
    "be an empty Links instance" in newCase[CaseData]: (tn, data) =>
      Links.empty.indexies.pure[IO].asserting(_ mustBe empty)

  "DcgEdgeSamples.Links.join(...)" should:
    "join two Links instances without conflicts" in newCase[CaseData]: (tn, data) =>
      data.links1.join[IO](Links(data.indexies2)).logValue(tn)
        .asserting(_.indexies mustBe (data.indexies1 ++ data.indexies2))

  "DcgEdgeSamples.Links.add(...)" should:
    "add new link sample without conflicts" in newCase[CaseData]: (tn, data) =>
      data.links1.add[IO](SampleId(30), HnIndex(5), HnIndex(6)).logValue(tn)
        .asserting(_ mustBe Links(data.indexies1 + (SampleId(30) -> Indexies(HnIndex(5), HnIndex(6)))))

  "DcgEdgeSamples.Thens.empty" should:
    "be an empty Thens instance" in newCase[CaseData]: (tn, data) =>
      Thens.empty.indexies.pure[IO].asserting(_ mustBe empty)

  "DcgEdgeSamples.Thens.join(...)" should:
    "join two Thens instances without conflicts" in newCase[CaseData]: (tn, data) =>
      Thens(data.indexies1).join[IO](Thens(data.indexies2)).logValue(tn)
        .asserting(_.indexies mustBe (data.indexies1 ++ data.indexies2))

  "DcgEdgeSamples.Thens.add(...)" should:
    "add new then sample without conflicts" in newCase[CaseData]: (tn, data) =>
      Thens(data.indexies1).add[IO](SampleId(30), HnIndex(5), HnIndex(6)).logValue(tn)
        .asserting(_ mustBe Thens(data.indexies1 + (SampleId(30) -> Indexies(HnIndex(5), HnIndex(6)))))
