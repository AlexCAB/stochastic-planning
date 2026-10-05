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
import cats.effect.cps.*
import planning.engine.api.model.map.{MapAddSamplesRequest, TestMpiData}
import planning.engine.api.model.map.extensions.mpi.MapAddSamplesRequestEx.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.model.data.node.NodeData

class MapAddSamplesRequestExSpec extends UnitSpecWithData:

  private class CaseData extends Case with TestMpiData:
    lazy val request = MapAddSamplesRequest(
      samples = List(testNewSampleData),
      hiddenNodes = List(testConNodeBoolDef, testAbsNodeDef1),
    )

    lazy val expectedNames = Map(
      testConNodeBoolDef.name -> MnId.Nim(0L),
      testAbsNodeDef1.name -> MnId.Nim(1L),
    )

    lazy val expectedNodes: Map[MnId.Nim, NodeData] = Map(
      MnId.Nim(0L) -> NodeData.Con(
        name = Some(testConNodeBoolDef.name),
        description = testConNodeBoolDef.description,
        ioName = testConNodeBoolDef.ioNodeName,
        valueIndex = IoIndex(1L),
      ),
      MnId.Nim(1L) -> NodeData.Abs(Some(testAbsNodeDef1.name), testAbsNodeDef1.description),
    )

  "MapAddSamplesRequestEx.toNodes(...)" should:
    "convert hidden nodes to node data keyed by Nim IDs" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val (names, nodes) = data.request.toNodes[IO](data.vars).logValue(tn).await

        names mustEqual data.expectedNames
        nodes mustEqual data.expectedNodes

    "fail if hidden node references undefined IO variable" in newCase[CaseData]: (tn, data) =>
      val unknownName = IoName("unknownVar")
      val request = data.request.copy(hiddenNodes = List(data.testConNodeBoolDef.copy(ioNodeName = unknownName)))

      request.toNodes[IO](data.vars).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Undefined IO name $unknownName"))

  "MapAddSamplesRequestEx.toSamples(...)" should:
    "convert samples to set of Sample.Man" in newCase[CaseData]: (tn, data) =>
      data.request.toSamples[IO](data.mnIds).logValue(tn)
        .asserting(_ mustEqual Set(data.sampleMan))

    "fail if sample edge references unknown HnName" in newCase[CaseData]: (tn, data) =>
      data.request.toSamples[IO](Map.empty).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("not found in mnIds"))

    "fail if samples are not distinct" in newCase[CaseData]: (tn, data) =>
      data.request.copy(samples = List(data.testNewSampleData, data.testNewSampleData))
        .toSamples[IO](data.mnIds).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Samples must be distinct"))
