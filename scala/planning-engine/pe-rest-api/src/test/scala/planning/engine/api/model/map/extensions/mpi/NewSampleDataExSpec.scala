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
| created: 2026-10-06 |||||||||||*/

package planning.engine.api.model.map.extensions.mpi

import cats.effect.IO
import planning.engine.api.model.map.TestMpiData
import planning.engine.api.model.map.extensions.mpi.NewSampleDataEx.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Name

class NewSampleDataExSpec extends UnitSpecWithData:
  private class CaseData extends Case with TestMpiData

  "NewSampleDataEx.toSample(...)" should:
    "convert NewSampleData to Sample.Man" in newCase[CaseData]: (tn, data) =>
      data.testNewSampleData.toSample[IO](data.mnIds).logValue(tn)
        .asserting(_ mustEqual data.sampleMan)

    "generate default name if sample name is not defined" in newCase[CaseData]: (tn, data) =>
      data.testNewSampleData.copy(name = None).toSample[IO](data.mnIds).logValue(tn)
        .asserting(_.info.name mustEqual Name("Unnamed Sample: P(10), U(0.5)"))

    "fail if edge HnName not found in MnId map" in newCase[CaseData]: (tn, data) =>
      val unknownName = HnName("unknownHn")
      val edge = data.testEdge.copy(targetHnName = unknownName)

      data.testNewSampleData.copy(edges = List(edge)).toSample[IO](data.mnIds).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"HnName $unknownName not found in mnIds"))

    "fail if sample edges are not distinct" in newCase[CaseData]: (tn, data) =>
      data.testNewSampleData.copy(edges = List(data.testEdge, data.testEdge)).toSample[IO](data.mnIds)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Sample edges must be distinct"))
