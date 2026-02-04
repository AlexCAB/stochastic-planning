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
| created: 2026-01-27 |||||||||||*/

package planning.engine.planner.map.dcg.samples

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.validation.ValidationCheck
import planning.engine.common.values.edges.Edge
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.test.data.MapSampleTestData

class DcgSampleSpec extends UnitSpecWithData with ValidationCheck:

  private class CaseData extends Case with MapSampleTestData:
    lazy val n1 = HnId(1)
    lazy val n2 = HnId(2)
    lazy val n3 = HnId(3)
    lazy val n4 = HnId(4)

    val sampleData = makeSampleData()
    val edges = Set(Edge.Link(n1, n2), Edge.Then(n2, n3), Edge.Link(n1, n4))

    val dcgSample = DcgSample(sampleData, edges)

  "DcgSample.validationName" should:
    "return correct name" in newCase[CaseData]: (tn, data) =>
      data.dcgSample.checkValidationName(s"DcgSample(id=${data.sampleData.id}, name=${data.sampleData.name.toStr})", tn)

  "DcgSample.validationErrors" should:
    "return no errors for valid sample" in newCase[CaseData]: (tn, data) =>
      data.dcgSample.checkNoValidationError(tn)

    "return error for non-connected edges" in newCase[CaseData]: (tn, data) =>
      val invalidEdges = Set(Edge.Link(data.n1, data.n2), Edge.Then(HnId(5), HnId(6)))

      data.dcgSample.copy(edges = invalidEdges)
        .checkOneValidationError("DcgSample edges must form a connected graph", tn)

  "DcgSample.repr" should:
    "return correct representation" in newCase[CaseData]: (tn, data) =>
      data.dcgSample.repr.pure[IO].logValue(tn).asserting(_ must include("DcgSample"))
