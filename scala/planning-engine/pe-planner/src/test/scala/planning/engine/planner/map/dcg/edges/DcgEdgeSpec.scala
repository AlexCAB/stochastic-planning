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
import planning.engine.planner.map.MapTestData
import planning.engine.planner.map.dcg.edges.DcgEdge.Indexies

class DcgEdgeSpec extends UnitSpecWithData with MapTestData:

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

    lazy val dcgEdge: DcgEdge[IO] = DcgEdge[IO](
      key = DcgEdge.Key(
        edgeType = hiddenEdge.edgeType,
        sourceId = hiddenEdge.sourceId,
        targetId = hiddenEdge.targetId
      ),
      samples = hiddenEdge.samples
        .map(s => s.sampleId -> Indexies(sourceIndex = s.sourceIndex, targetIndex = s.targetIndex))
        .toMap
    )

  "DcgEdge.hnIds" should:
    "return correct set of HnIds" in newCase[CaseData]: (tn, data) =>
      data.dcgEdge.pure[IO].logValue(tn).asserting(_.hnIds mustBe Set(
        data.hiddenEdge.sourceId,
        data.hiddenEdge.targetId
      ))

  "DcgEdge.join" should:
    def makeOtherDcgEdge(edge: DcgEdge[IO]): DcgEdge[IO] = edge
      .copy(samples = Map(SampleId(-12) -> Indexies(HnIndex(-203), HnIndex(-204))))

    "join two DcgEdges correctly" in newCase[CaseData]: (tn, data) =>
      val otherDcgEdge = makeOtherDcgEdge(data.dcgEdge)

      data.dcgEdge.join(otherDcgEdge).logValue(tn).asserting: joined =>
        joined.key mustBe data.dcgEdge.key
        joined.samples mustBe (data.dcgEdge.samples ++ otherDcgEdge.samples)

    "fail if key not match" in newCase[CaseData]: (tn, data) =>
      val otherDcgEdge: DcgEdge[IO] = makeOtherDcgEdge(data.dcgEdge)
        .copy(key = data.dcgEdge.key.copy(sourceId = HnId(-1)))

      data.dcgEdge.join(otherDcgEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Cannot join with different keys"))

    "fail if duplicate sampleIds" in newCase[CaseData]: (tn, data) =>
      data.dcgEdge.join(data.dcgEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map edge can't have duplicate samples"))

  "DcgEdge.apply(HiddenEdge)" should:
    "crete DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (tn, data) =>
      DcgEdge[IO](data.hiddenEdge).logValue(tn).asserting(_ mustBe data.dcgEdge)

  "DcgEdge.apply(SampleEdge)" should:
    "crete DcgEdge correctly from SampleEdge" in newCase[CaseData]: (tn, data) =>
      DcgEdge[IO](data.sampleEdge).logValue(tn).asserting(_ mustBe data.dcgEdge)
