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
import planning.engine.common.graph.edges.{EdgeKey, IndexMap, Indexies}
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies

class DcgSamplesSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val conN1 = MnId.Con(1)
    lazy val absN2 = MnId.Abs(2)
    lazy val keyLink1 = EdgeKey.Link(conN1, absN2)

    def makeDcgSamples(sId: Int, srcInd: Int, trgInd: Int): DcgSamples[IO] =
      DcgSamples[IO](Map(SampleId(sId) -> Indexies(HnIndex(srcInd), HnIndex(trgInd)))).unsafeRunSync()

    lazy val samples1: DcgSamples[IO] = makeDcgSamples(sId = 10, srcInd = 1, trgInd = 1)
    lazy val samples2: DcgSamples[IO] = makeDcgSamples(sId = 20, srcInd = 2, trgInd = 3)
    lazy val samples3: DcgSamples[IO] = makeDcgSamples(sId = 30, srcInd = 1, trgInd = 4)
    lazy val samples4: DcgSamples[IO] = makeDcgSamples(sId = 40, srcInd = 5, trgInd = 1)

    lazy val (samplesId1, indexies1) = samples1.indexies.head
    lazy val sampleIndexies1: SampleIndexies = SampleIndexies(samplesId1, indexies1.src, indexies1.trg)

    lazy val indexMap1: Map[SampleId, IndexMap] = Map(
      samplesId1 -> IndexMap(Map(conN1 -> indexies1.src, absN2 -> indexies1.trg))
    )

  "DcgSamples.size" should:
    "return correct size of indexies map" in newCase[CaseData]: (tn, data) =>
      data.samples1.pure[IO].logValue(tn).asserting(_.size mustBe 1)

  "DcgSamples.isEmpty" should:
    "return false if non empty" in newCase[CaseData]: (tn, data) =>
      data.samples1.isEmpty.pure[IO].asserting(_ mustBe false)

    "return true if empty" in newCase[CaseData]: (tn, _) =>
      DcgSamples.empty[IO].isEmpty.pure[IO].asserting(_ mustBe true)

  "DcgSamples.srcHnIndex" should:
    "return correct set of source HnIndexes" in newCase[CaseData]: (tn, data) =>
      data.samples1.srcHnIndex.pure[IO].logValue(tn).asserting(_ mustBe Set(HnIndex(1)))

  "DcgSamples.trgHnIndex" should:
    "return correct set of target HnIndexes" in newCase[CaseData]: (tn, data) =>
      data.samples1.trgHnIndex.pure[IO].logValue(tn).asserting(_ mustBe Set(HnIndex(1)))

  "DcgSamples.join(...)" should:
    "join two index maps without conflicts" in newCase[CaseData]: (tn, data) =>
      import data.{samples1, samples2}
      samples1.join(samples2).logValue(tn).asserting(_.indexies mustBe (samples1.indexies ++ samples2.indexies))

    "fail to join two index maps with conflicts" in newCase[CaseData]: (tn, data) =>
      import data.samples1
      samples1.join(samples1).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate sample"))

    "fail to join two index maps with source index conflicts" in newCase[CaseData]: (tn, data) =>
      import data.{samples1, samples3}
      samples1.join(samples3).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate source indexes"))

    "fail to join two index maps with target index conflicts" in newCase[CaseData]: (tn, data) =>
      import data.{samples1, samples4}
      samples1.join(samples4).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate target indexes"))

  "DcgSamples.empty" should:
    "return empty DcgSamples" in newCase[CaseData]: (tn, _) =>
      DcgSamples.empty[IO].pure[IO].logValue(tn).asserting(_.indexies mustBe Map.empty)

  "DcgSamples.apply(SampleId, HnIndex, HnIndex)" should:
    "create DcgSamples with single indexies" in newCase[CaseData]: (tn, data) =>
      import data.{samplesId1, indexies1, samples1}
      DcgSamples[IO](samplesId1, indexies1.src, indexies1.trg).pure[IO].logValue(tn).asserting(_ mustBe samples1)

  "DcgSamples.apply(Map[SampleId, Indexies])" should:
    "create DcgSamples from indexies map" in newCase[CaseData]: (tn, data) =>
      import data.samples1
      DcgSamples[IO](samples1.indexies).logValue(tn).asserting(_ mustBe samples1)

    "fail to create DcgSamples with duplicate source indexes" in newCase[CaseData]: (tn, data) =>
      import data.{samples1, samples3}
      DcgSamples[IO](samples1.indexies ++ samples3.indexies).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate source index"))

    "fail to create DcgSamples with duplicate target indexes" in newCase[CaseData]: (tn, data) =>
      import data.{samples1, samples4}
      DcgSamples[IO](samples1.indexies ++ samples4.indexies).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate target index"))

  "DcgSamples.fromSamples" should:
    "create DcgSamples from SampleIndexies" in newCase[CaseData]: (tn, data) =>
      import data.{sampleIndexies1, samples1}
      DcgSamples.fromSamples[IO](List(sampleIndexies1)).logValue(tn).asserting(_ mustBe samples1)

    "fail to create DcgSamples with duplicate samples" in newCase[CaseData]: (tn, data) =>
      import data.sampleIndexies1
      DcgSamples.fromSamples[IO](List(sampleIndexies1, sampleIndexies1)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate sample"))

  "DcgSamples.fromIndexMap" should:
    "create DcgSamples from EdgeKey and Map[SampleId, IndexMap]" in newCase[CaseData]: (tn, data) =>
      import data.{keyLink1, indexMap1, samples1}
      DcgSamples.fromIndexMap[IO](keyLink1, indexMap1).logValue(tn).asserting(_ mustBe samples1)
