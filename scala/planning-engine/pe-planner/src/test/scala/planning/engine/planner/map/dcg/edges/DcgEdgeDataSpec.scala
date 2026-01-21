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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.map.samples.sample.SampleEdge.End
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.{Indexies, Links, Thens}

class DcgEdgeDataSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val hiddenLinkEdge: HiddenEdge = HiddenEdge(
      edgeType = EdgeType.LINK,
      sourceId = HnId(1),
      targetId = HnId(2),
      samples = List(SampleIndexies(sampleId = SampleId(11), sourceIndex = HnIndex(201), targetIndex = HnIndex(202)))
    )

    lazy val hiddenThenEdge: HiddenEdge = hiddenLinkEdge.copy(
      edgeType = EdgeType.THEN,
      samples = List(SampleIndexies(sampleId = SampleId(12), sourceIndex = HnIndex(203), targetIndex = HnIndex(204)))
    )

    lazy val sampleLinkEdge: SampleEdge = SampleEdge(
      sampleId = hiddenLinkEdge.samples.head.sampleId,
      source = SampleEdge.End(
        hnId = hiddenLinkEdge.sourceId,
        value = hiddenLinkEdge.samples.head.sourceIndex
      ),
      target = SampleEdge.End(
        hnId = hiddenLinkEdge.targetId,
        value = hiddenLinkEdge.samples.head.targetIndex
      ),
      edgeType = hiddenLinkEdge.edgeType
    )

    def makeSamples(edge: HiddenEdge): Map[SampleId, Indexies] = edge.samples
      .map(s => s.sampleId -> Indexies(s.sourceIndex, s.targetIndex))
      .toMap

    lazy val linkSamples: Map[SampleId, Indexies] = makeSamples(hiddenLinkEdge)
    lazy val thenSamples: Map[SampleId, Indexies] = makeSamples(hiddenThenEdge)

    lazy val dcgLinkEdge: DcgEdgeData = DcgEdgeData(
      ends = EndIds(hiddenLinkEdge.sourceId, hiddenLinkEdge.targetId),
      links = Links(linkSamples),
      thens = Thens.empty
    )

    lazy val dcgThenEdge: DcgEdgeData = DcgEdgeData(
      ends = EndIds(hiddenThenEdge.sourceId, hiddenThenEdge.targetId),
      links = Links.empty,
      thens = Thens(thenSamples)
    )

    lazy val ends: EndIds = dcgLinkEdge.ends
    lazy val edgeType: EdgeType = hiddenLinkEdge.edgeType

  "DcgEdgeData.hnIds" should:
    "return correct set of HnIds" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.pure[IO].logValue(tn).asserting(_.hnIds mustBe Set(
        data.hiddenLinkEdge.sourceId,
        data.hiddenLinkEdge.targetId
      ))

  "DcgEdgeData.linksIds" should:
    "return correct set of SampleIds for links" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.pure[IO].logValue(tn).asserting(_.linksIds mustBe data.linkSamples.keySet)

  "DcgEdgeData.thensIds" should:
    "return correct set of SampleIds for thens" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.pure[IO].logValue(tn).asserting(_.thensIds mustBe data.thenSamples.keySet)

  "DcgEdgeData.sampleIds" should:
    "return correct set of SampleIds" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.copy(thens = Thens(data.thenSamples)).pure[IO].logValue(tn)
        .asserting(_.sampleIds mustBe (data.linkSamples.keySet ++ data.thenSamples.keySet))

  "DcgEdgeData.isLink" should:
    "return true for link edge" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.pure[IO].logValue(tn).asserting(_.isLink mustBe true)

    "return false for then edge" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.pure[IO].logValue(tn).asserting(_.isLink mustBe false)

  "DcgEdgeData.isThen"  should:
    "return true for then edge" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.pure[IO].logValue(tn).asserting(_.isThen mustBe true)

    "return false for link edge" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.pure[IO].logValue(tn).asserting(_.isThen mustBe false)

  "DcgEdgeData.join(...)" should:
    def makeOtherDcgEdge(edge: DcgEdgeData): DcgEdgeData = edge
      .copy(links = Links(Map(SampleId(-12) -> Indexies(HnIndex(-203), HnIndex(-204)))))

    "join two DcgEdges correctly" in newCase[CaseData]: (tn, data) =>
      val otherDcgEdge = makeOtherDcgEdge(data.dcgLinkEdge)

      data.dcgLinkEdge.join[IO](otherDcgEdge).logValue(tn).asserting: joined =>
        joined.ends mustBe data.dcgLinkEdge.ends
        joined.links mustBe Links(data.dcgLinkEdge.links.indexies ++ otherDcgEdge.links.indexies)

    "fail if key not match" in newCase[CaseData]: (tn, data) =>
      val otherDcgEdge: DcgEdgeData = makeOtherDcgEdge(data.dcgLinkEdge)
        .copy(ends = data.dcgLinkEdge.ends.copy(src = HnId(-1)))

      data.dcgLinkEdge.join[IO](otherDcgEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Cannot join edges with different ends"))

    "fail if duplicate sample IDs in links" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.join[IO](data.dcgLinkEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate links sample"))

    "fail if duplicate sample IDs in thens" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.join[IO](data.dcgThenEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate thens sample"))

  "DcgEdgeData.EndIds.swap" should:
    "swap ends correctly" in newCase[CaseData]: (tn, data) =>
      data.ends.swap.pure[IO].logValue(tn).asserting(_ mustBe EndIds(data.ends.trg, data.ends.src))

  "DcgEdgeData.makeDcgEdgeData" should:
    "crete DcgEdge correctly for Link edge type" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData.makeDcgEdgeData(data.ends, EdgeType.LINK, data.linkSamples).pure[IO].logValue(tn)
        .asserting(_ mustBe data.dcgLinkEdge)

    "crete DcgEdge correctly for Then edge type" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData.makeDcgEdgeData(data.ends, EdgeType.THEN, data.thenSamples).pure[IO].logValue(tn)
        .asserting(_ mustBe data.dcgThenEdge)

  "DcgEdgeData.apply(HiddenEdge)" should:
    "crete DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData(data.hiddenLinkEdge).pure[IO].logValue(tn).asserting(_ mustBe data.dcgLinkEdge)

  "DcgEdgeData.apply(Key, List[SampleEdge])" should:
    "crete DcgEdge correctly from Key, List[SampleEdge]" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleLinkEdge))
        .logValue(tn).asserting(_ mustBe data.dcgLinkEdge)

    "fail if key not match" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData
        .apply[IO](data.edgeType, data.ends, List(data.sampleLinkEdge.copy(source = End(HnId(-1), HnIndex(-1)))))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge keys from SampleEdges do not"))

    "fail if duplicate SampleIds" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleLinkEdge, data.sampleLinkEdge)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate SampleIds in SampleEdges"))

    "fail if empty SampleEdges" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData[IO](data.edgeType, data.ends, List()).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("SampleEdges list is empty"))

    "fail if Duplicate Source value" in newCase[CaseData]: (tn, data) =>
      val sampleEdge2 = data.sampleLinkEdge.copy(sampleId = SampleId(12))
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleLinkEdge, sampleEdge2)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Source value in SampleEdges"))

    "fail if Duplicate Target value" in newCase[CaseData]: (tn, data) =>
      val sampleEdge2 = data.sampleLinkEdge.copy(
        sampleId = SampleId(12),
        source = End(data.sampleLinkEdge.source.hnId, HnIndex(999)),
        target = data.sampleLinkEdge.target
      )
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleLinkEdge, sampleEdge2)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Target value in SampleEdges"))
