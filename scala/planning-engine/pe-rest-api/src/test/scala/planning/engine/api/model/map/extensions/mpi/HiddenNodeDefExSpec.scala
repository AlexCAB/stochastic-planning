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
import io.circe.Json
import planning.engine.api.model.map.TestMpiData
import planning.engine.api.model.map.extensions.mpi.HiddenNodeDefEx.*
import planning.engine.api.model.map.payload.ConcreteNodeDef
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description
import planning.engine.planner.mpi.model.data.node.NodeData

class HiddenNodeDefExSpec extends UnitSpecWithData:

  private class CaseData extends Case with TestMpiData:
    def expectedCon(conDef: ConcreteNodeDef, index: Long): NodeData =
      NodeData.Con(Some(conDef.name), conDef.description, conDef.ioNodeName, IoIndex(index))

  "HiddenNodeDefEx.toNodeData(...)" should:
    "convert AbstractNodeDef to NodeData.Abs" in newCase[CaseData]: (tn, data) =>
      data.testAbsNodeDef1.toNodeData(data.vars)[IO].logValue(tn)
        .asserting(_ mustEqual NodeData.Abs(Some(HnName("absHn3")), Description.some("testAbsNodeDef1")))

    "convert ConcreteNodeDef with boolean input variable" in newCase[CaseData]: (tn, data) =>
      data.testConNodeBoolDef.toNodeData(data.vars)[IO].logValue(tn)
        .asserting(_ mustEqual data.expectedCon(data.testConNodeBoolDef, 1L))

    "convert ConcreteNodeDef with float input variable" in newCase[CaseData]: (tn, data) =>
      data.testConNodeFloatDef.toNodeData(data.vars)[IO].logValue(tn)
        .asserting(_ mustEqual data.expectedCon(data.testConNodeFloatDef, 5000L))

    "convert ConcreteNodeDef with integer output variable" in newCase[CaseData]: (tn, data) =>
      data.testConNodeIntDef.toNodeData(data.vars)[IO].logValue(tn)
        .asserting(_ mustEqual data.expectedCon(data.testConNodeIntDef, 5L))

    "convert ConcreteNodeDef with list of strings output variable" in newCase[CaseData]: (tn, data) =>
      data.testConNodeListStrDef.toNodeData(data.vars)[IO].logValue(tn)
        .asserting(_ mustEqual data.expectedCon(data.testConNodeListStrDef, 0L))

    "fail if IO variable is not defined" in newCase[CaseData]: (tn, data) =>
      val unknownName = IoName("unknownVar")

      data.testConNodeIntDef.copy(ioNodeName = unknownName).toNodeData(data.vars)[IO].logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Undefined IO name $unknownName"))

    "fail if value can't be decoded as variable type" in newCase[CaseData]: (tn, data) =>
      data.testConNodeIntDef.copy(value = Json.fromString("not a number")).toNodeData(data.vars)[IO].logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Failed to decode value"))

    "fail if value is not acceptable by variable type" in newCase[CaseData]: (tn, data) =>
      data.testConNodeIntDef.copy(value = Json.fromLong(11L)).toNodeData(data.vars)[IO].logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Value 11 not in range [0, 10]"))
