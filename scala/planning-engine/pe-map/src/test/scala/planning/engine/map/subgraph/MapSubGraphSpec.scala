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
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.map.MapGraphTestData

class MapSubGraphSpec extends UnitSpecWithData with MapGraphTestData with ValidationCheck:

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
      data.subGraph.checkValidationName("MapSubGraph(nodes=", tn)

  "MapSubGraph.validationErrors" should:
    "return no validation errors for valid subgraph" in newCase[CaseData]: (tn, data) =>
      data.subGraph.checkNoValidationError(tn)

    "return errors for duplicate concrete node" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(concreteNodes = List(data.conNodes.head, data.conNodes.head))
        .checkOneOfValidationErrors("Concrete node IDs must be distinct, duplicates", tn)

    "return errors for duplicate abstract node" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(abstractNodes = List(data.absNodes.head, data.absNodes.head))
        .checkOneOfValidationErrors("Abstract node IDs must be distinct, duplicates", tn)

    "return errors for duplicate node IDs" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(abstractNodes = data.absNodes :+ makeAbstractNode(data.conNodes.head.id.value))
        .checkOneValidationError("Node IDs must be distinct, duplicates", tn)

    "return errors for duplicate edge keys" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(edges = List(data.edges.head, data.edges.head))
        .checkOneOfValidationErrors("Edge keys (type, sourceId, targetId) must be distinct", tn)

    "return errors for missing edge node IDs" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = makeHiddenEdge(HnId(-1), HnId(-2), List(SampleId(1001L)), EdgeType.LINK)
      data.subGraph.copy(edges = data.edges :+ invalidEdge)
        .checkOneValidationError("All Edge node IDs must exist in concrete or abstract", tn)

    "return errors for duplicate sample IDs" in newCase[CaseData]: (tn, data) =>
      data.subGraph.copy(loadedSamples = data.samples :+ data.samples.head)
        .checkOneValidationError("Sample IDs must be distinct, duplicates", tn)

    "return errors for mismatched edge sample IDs" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = makeHiddenEdge(HnId(1), HnId(4), List(SampleId(-1)), EdgeType.LINK)

      data.subGraph.copy(edges = data.edges :+ invalidEdge)
        .checkOneValidationError("All edge sample IDs must exist in loaded or skipped samples, not same elements", tn)
