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
| created: 2025-12-14 |||||||||||*/

package planning.engine.map.subgraph

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.map.MapGraphTestData

class MapSubGraphSpec extends UnitSpecWithData with MapGraphTestData:

  private class CaseData extends Case:
    lazy val conNodes = List((1, 11), (2, 12), (3, 13)).map((i, v) => makeConcreteNode(i, v, boolInNode))
    lazy val absNodes = List(4, 5).map(i => makeAbstractNode(i))
    lazy val samples = List(1001, 1002).map(i => testSampleData.copy(id = SampleId(i)))
    lazy val sampleIds = List(1003, 1004).map(i => SampleId(i))

    lazy val edges = List((1, 2, List(1001L, 1003L), EdgeType.LINK), (2, 3, List(1002L, 1004L), EdgeType.THEN)).map:
      case (sId, tId, sm, k) => makeHiddenEdge(HnId(sId), HnId(tId), sm.map(SampleId.apply), k)

    lazy val subGraph: MapSubGraph[IO] = MapSubGraph[IO](conNodes, absNodes, edges, samples, sampleIds)

  "MapSubGraph.allIoValues" should:
    "return all IoValues from concrete nodes" in newCase[CaseData]: (tn, data) =>
      data.subGraph.allIoValues.pure[IO]
        .logValue(tn, "MapSubGraph.allIoValues")
        .asserting(_ mustEqual data.conNodes.map(_.ioValue).toSet)

  "MapSubGraph.allSampleIds" should:
    "return all SampleIds from samples and edges" in newCase[CaseData]: (tn, data) =>
      data.subGraph.allSampleIds.pure[IO]
        .logValue(tn, "MapSubGraph.allSampleIds")
        .asserting(_ mustEqual (data.samples.map(_.id).toSet ++ data.sampleIds.toSet))

  "MapSubGraph.validationName" should:
    "return correct validation name" in newCase[CaseData]: (tn, data) =>
      data.subGraph.validationName.pure[IO]
        .logValue(tn, "MapSubGraph.validationName")
        .asserting(_ mustEqual "MapSubGraph(nodes=List(HnId(1), HnId(2), HnId(3), HnId(4), HnId(5)))")

  "MapSubGraph.validationErrors" should:
    "return no validation errors for valid subgraph" in newCase[CaseData]: (tn, data) =>
      data.subGraph.validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors")
        .asserting(_ mustBe Nil)

    "return errors for duplicate concrete node" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(concreteNodes = List(data.conNodes.head, data.conNodes.head)).validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - duplicate concrete node")
        .asserting(_.map(_.getMessage) must contain("Concrete node IDs must be distinct, duplicates: HnId(1)"))

    "return errors for duplicate abstract node" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(abstractNodes = List(data.absNodes.head, data.absNodes.head)).validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - duplicate abstract node")
        .asserting(_.map(_.getMessage) must contain("Abstract node IDs must be distinct, duplicates: HnId(4)"))

    "return errors for duplicate node IDs" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(abstractNodes = data.absNodes :+ makeAbstractNode(data.conNodes.head.id.value))
        .validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - duplicate node IDs")
        .asserting(_.map(_.getMessage) must contain(s"Node IDs must be distinct, duplicates: HnId(1)"))

    "return errors for duplicate edge keys" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(edges = List(data.edges.head, data.edges.head)).validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - duplicate edge keys")
        .asserting(_.map(_.getMessage) must contain(
          s"Edge keys (type, sourceId, targetId) must be distinct, duplicates: (LINK,HnId(1),HnId(2))"
        ))

    "return errors for missing edge node IDs" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = makeHiddenEdge(HnId(-1), HnId(-2), List(SampleId(1001L)), EdgeType.LINK)
      data.subGraph.copy(edges = data.edges :+ invalidEdge).validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - missing edge node IDs")
        .asserting(_.map(_.getMessage) must contain(
          s"All Edge node IDs must exist in concrete or abstract nodes list, missing elements: HnId(-1), HnId(-2)"
        ))

    "return errors for duplicate sample IDs" in newCase[CaseData]: (tn,data) =>
      data.subGraph.copy(loadedSamples = data.samples :+ data.samples.head).validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - duplicate sample IDs")
        .asserting(_.map(_.getMessage) must contain(
          s"Sample IDs must be distinct, duplicates: SampleId(1001)"
        ))

    "return errors for mismatched edge sample IDs" in newCase[CaseData]: (tn,data) =>
      val invalidEdge = makeHiddenEdge(HnId(1), HnId(4), List(SampleId(-1)), EdgeType.LINK)

      data.subGraph.copy(edges = data.edges :+ invalidEdge).validationErrors.pure[IO]
        .logValue(tn, "MapSubGraph.validationErrors - mismatched edge sample IDs")
        .asserting(_.map(_.getMessage) must contain(
          s"All edge sample IDs must exist in loaded or skipped samples, not same elements: SampleId(-1)"
        ))
