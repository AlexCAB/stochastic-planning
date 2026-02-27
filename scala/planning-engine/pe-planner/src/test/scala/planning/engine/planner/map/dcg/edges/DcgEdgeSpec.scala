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
import planning.engine.common.enums.EdgeType.{LINK, THEN}
import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.edges.{EdgeKey, IndexMap, Indexies}
import planning.engine.common.values.node.{MnId, HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies

class DcgEdgeSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val n1 = HnId(1)
    lazy val n2 = HnId(2)
    lazy val conN1 = n1.asCon
    lazy val absN2 = n2.asAbs

    def makeHiddenEdge(et: EdgeType, sId: Int): HiddenEdge = HiddenEdge(
      edgeType = et,
      sourceId = n1,
      targetId = n2,
      samples = List(SampleIndexies(SampleId(sId), sourceIndex = HnIndex(sId + 200), targetIndex = HnIndex(sId + 201)))
    )

    lazy val heLink: HiddenEdge = makeHiddenEdge(LINK, sId = 11)
    lazy val heThen1: HiddenEdge = makeHiddenEdge(THEN, sId = 21)
    lazy val heThen2: HiddenEdge = makeHiddenEdge(THEN, sId = 22)
    
    def makeKey(et: EdgeType, src: MnId, trg: MnId): EdgeKey = et match
      case LINK => EdgeKey.Link(src, trg)
      case THEN => EdgeKey.Then(src, trg)

    def makeDcgEdge(edge: HiddenEdge): DcgEdge[IO] = DcgEdge[IO](
      key = makeKey(edge.edgeType, edge.sourceId.asCon, edge.targetId.asAbs),
      samples = DcgSamples[IO](
        edge.samples.map(s => s.sampleId -> Indexies(s.sourceIndex, s.targetIndex)).toMap
      ).unsafeRunSync()
    )

    lazy val linkEdge: DcgEdge[IO] = makeDcgEdge(heLink)
    lazy val thenEdge1: DcgEdge[IO] = makeDcgEdge(heThen1)
    lazy val thenEdge2: DcgEdge[IO] = makeDcgEdge(heThen2)

    lazy val linkSampleId: SampleId = linkEdge.samples.sampleIds.head

    lazy val linkIndexiesMap: Map[SampleId, IndexMap] = heLink.samples
      .map(ind => ind.sampleId -> IndexMap(Map(conN1 -> ind.sourceIndex, absN2 -> ind.targetIndex))).toMap

  "DcgEdgeData.mnIds" should:
    "return correct set of HnIds" in newCase[CaseData]: (tn, data) =>
      import data.{linkEdge, conN1, absN2}
      linkEdge.pure[IO].logValue(tn).asserting(_.mnIds mustBe Set(conN1, absN2))

  "DcgEdgeData.edgeType" should:
    "return correct EdgeType for link edge" in newCase[CaseData]: (tn, data) =>
      data.linkEdge.pure[IO].logValue(tn).asserting(_.edgeType mustBe EdgeType.LINK)

    "return correct EdgeType for then edge" in newCase[CaseData]: (tn, data) =>
      data.thenEdge1.pure[IO].logValue(tn).asserting(_.edgeType mustBe EdgeType.THEN)

  "DcgEdgeData.join(...)" should:
    "join two DcgEdges correctly" in newCase[CaseData]: (tn, data) =>
      import data.{thenEdge1, thenEdge2}
      thenEdge1.join(thenEdge2).logValue(tn).asserting: joined =>
        joined.key mustBe thenEdge1.key
        joined.key mustBe thenEdge2.key
        joined.samples.indexies mustBe (thenEdge1.samples.indexies ++ thenEdge2.samples.indexies)

    "fail if key not match" in newCase[CaseData]: (tn, data) =>
      import data.{thenEdge1, linkEdge}
      thenEdge1.join(linkEdge).logValue(tn).assertThrowsError[AssertionError](_
        .getMessage must include("Can't join DcgEdges with different keys"))

  "DcgEdgeData.isActive" should:
    "return true if at least one sampleId is active" in newCase[CaseData]: (tn, data) =>
      import data.{linkEdge, linkSampleId}
      linkEdge.isActive(Set(linkSampleId)).pure[IO].logValue(tn).asserting(_ mustBe true)

    "return false if no sampleId is active" in newCase[CaseData]: (tn, data) =>
      import data.{thenEdge1, linkSampleId}
      thenEdge1.isActive(Set(linkSampleId)).pure[IO].logValue(tn).asserting(_ mustBe false)

  "DcgEdgeData.apply(HiddenEdge)" should:
    "crete LINK DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (tn, data) =>
      import data.{heLink, linkEdge, conN1, absN2}
      DcgEdge[IO](heLink, Set(conN1), Set(absN2)).logValue(tn).asserting(_ mustBe linkEdge)

    "crete THEN DcgEdge correctly from HiddenEdge" in newCase[CaseData]: (tn, data) =>
      import data.{heThen1, thenEdge1, conN1, absN2}
      DcgEdge[IO](heThen1, Set(conN1), Set(absN2)).logValue(tn).asserting(_ mustBe thenEdge1)

  "DcgEdgeData.apply(EdgeKey, Map[SampleId, IndexMap])" should:
    "crete DcgEdge correctly from EdgeKey and Map[SampleId, IndexMap]" in newCase[CaseData]: (tn, data) =>
      import data.{linkEdge, linkIndexiesMap}
      DcgEdge[IO](linkEdge.key, linkIndexiesMap).logValue(tn).asserting(_ mustBe linkEdge)
