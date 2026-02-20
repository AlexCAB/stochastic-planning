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
import planning.engine.common.graph.GraphStructure
import planning.engine.common.values.edge.{EdgeKey, IndexMap}
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.planner.map.test.data.MapSampleTestData

class DcgSampleSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapSampleTestData:
    lazy val n1 = MnId.Con(1)
    lazy val n2 = MnId.Con(2)
    lazy val n3 = MnId.Abs(3)
    lazy val n4 = MnId.Abs(4)

    lazy val conNodes = Set(n1, n2)
    lazy val absNodes = Set(n3, n4)

    lazy val sampleData = makeSampleData()

    lazy val edges = Set(EdgeKey.Link(n1, n2), EdgeKey.Then(n2, n3), EdgeKey.Link(n1, n4))
    lazy val structure = GraphStructure[IO](edges)
    lazy val indexMap = IndexMap(Map(n1 -> HnIndex(11), n2 -> HnIndex(12), n3 -> HnIndex(13), n4 -> HnIndex(14)))

    lazy val dcgSample = new DcgSample[IO](sampleData, structure)
    lazy val dcgSampleAdd = DcgSample.Add[IO](dcgSample, indexMap)

    lazy val newSampleData = makeNewSampleData(sampleData, edges)

  "DcgSample.Add.idsByKey" should:
    "return correct set of ids by edge keys" in newCase[CaseData]: (tn, data) =>
      import data.{dcgSampleAdd, edges, sampleData, indexMap}
      dcgSampleAdd.idsByKey.pure[IO].logValue(tn).asserting(_ mustBe edges.map(k => k -> (sampleData.id, indexMap)))

  "DcgSample.apply(SampleId, Sample.New, ...)" should:
    "create DcgSample with correct data and structure" in newCase[CaseData]: (tn, data) =>
      import data.*
      DcgSample[IO](sampleData.id, newSampleData, conNodes, absNodes)
        .logValue(tn).asserting(_ mustBe dcgSample)

  "DcgSample.apply(SampleData, GraphStructure)" should:
    "return reate valid sample" in newCase[CaseData]: (tn, data) =>
      import data.{sampleData, structure, dcgSample}
      DcgSample[IO](sampleData, structure).logValue(tn).asserting(_ mustBe dcgSample)

    "return error if probability count not positive" in newCase[CaseData]: (tn, data) =>
      import data.{sampleData, structure}
      DcgSample[IO](sampleData.copy(probabilityCount = 0), structure).logValue(tn)
        .assertThrowsError(_.getMessage must include("Sample probability count must be positive"))

    "return error for non-connected edges" in newCase[CaseData]: (tn, data) =>
      import data.sampleData
      val invalidEdges = Set(EdgeKey.Link(data.n1, data.n2), EdgeKey.Then(MnId.Con(5), MnId.Con(6)))

      DcgSample[IO](sampleData, GraphStructure[IO](invalidEdges)).logValue(tn)
        .assertThrowsError(_.getMessage must include("DcgSample edges must form a connected graph"))
