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
    lazy val hiddenEdge: HiddenEdge = HiddenEdge(
      edgeType = EdgeType.LINK,
      sourceId = HnId(1),
      targetId = HnId(2),
      samples = List(SampleIndexies(
        sampleId = SampleId(11),
        sourceIndex = HnIndex(201),
        targetIndex = HnIndex(202)
      ))
    )

    lazy val sampleEdge: SampleEdge = SampleEdge(
      sampleId = hiddenEdge.samples.head.sampleId,
      source = SampleEdge.End(
        hnId = hiddenEdge.sourceId,
        value = hiddenEdge.samples.head.sourceIndex
      ),
      target = SampleEdge.End(
        hnId = hiddenEdge.targetId,
        value = hiddenEdge.samples.head.targetIndex
      ),
      edgeType = hiddenEdge.edgeType
    )

    lazy val samples: Map[SampleId, Indexies] = hiddenEdge.samples
      .map(s => s.sampleId -> Indexies(s.sourceIndex, s.targetIndex))
      .toMap

    lazy val dcgLinkEdge: DcgEdgeData = DcgEdgeData(
      ends = EndIds(hiddenEdge.sourceId, hiddenEdge.targetId),
      links = Links(samples),
      thens = Thens.empty
    )

    lazy val dcgThenEdge: DcgEdgeData = DcgEdgeData(
      ends = EndIds(hiddenEdge.sourceId, hiddenEdge.targetId),
      links = Links.empty,
      thens = Thens(samples)
    )

    lazy val ends: EndIds = dcgLinkEdge.ends
    lazy val edgeType: EdgeType = sampleEdge.edgeType

  "DcgEdge.hnIds" should:
    "return correct set of HnIds" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.pure[IO].logValue(tn).asserting(_.hnIds mustBe Set(
        data.hiddenEdge.sourceId,
        data.hiddenEdge.targetId
      ))

  "DcgEdge.linksIds" should:
    "return correct set of SampleIds for links" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.pure[IO].logValue(tn).asserting(_.linksIds mustBe data.samples.keySet)

  "DcgEdge.thensIds" should:
    "return correct set of SampleIds for thens" in newCase[CaseData]: (tn, data) =>
      data.dcgLinkEdge.copy(links = Links.empty, thens = Thens(data.samples)).pure[IO].logValue(tn)
        .asserting(_.thensIds mustBe data.samples.keySet)

  "DcgEdge.join" should:
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
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate links samples"))

    "fail if duplicate sample IDs in thens" in newCase[CaseData]: (tn, data) =>
      data.dcgThenEdge.join[IO](data.dcgThenEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate thens samples"))

  "DcgEdge.apply(HiddenEdge)" should:
    "crete DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData(data.hiddenEdge).pure[IO].logValue(tn).asserting(_ mustBe data.dcgLinkEdge)

  "DcgEdge.apply(Key, List[SampleEdge])" should:
    "crete DcgEdge correctly from Key, List[SampleEdge]" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleEdge))
        .logValue(tn).asserting(_ mustBe data.dcgLinkEdge)

    "fail if key not match" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData
        .apply[IO](data.edgeType, data.ends, List(data.sampleEdge.copy(source = End(HnId(-1), HnIndex(-1)))))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge keys from SampleEdges do not"))

    "fail if duplicate SampleIds" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleEdge, data.sampleEdge)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate SampleIds in SampleEdges"))

    "fail if empty SampleEdges" in newCase[CaseData]: (tn, data) =>
      DcgEdgeData[IO](data.edgeType, data.ends, List()).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("SampleEdges list is empty"))

    "fail if Duplicate Source value" in newCase[CaseData]: (tn, data) =>
      val sampleEdge2 = data.sampleEdge.copy(sampleId = SampleId(12))
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleEdge, sampleEdge2)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Source value in SampleEdges"))

    "fail if Duplicate Target value" in newCase[CaseData]: (tn, data) =>
      val sampleEdge2 = data.sampleEdge.copy(
        sampleId = SampleId(12),
        source = End(data.sampleEdge.source.hnId, HnIndex(999)),
        target = data.sampleEdge.target
      )
      DcgEdgeData[IO](data.edgeType, data.ends, List(data.sampleEdge, sampleEdge2)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Target value in SampleEdges"))
