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
| created: 03.09.26 |||||||||||||*/

package planning.engine.planner.mpi.model.io

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.test.data.MapNodeTestData

class IoVarsSpec extends UnitSpecWithData with MapNodeTestData:
  private class CaseData extends Case with WithMapNode:
    val undefinedNode: Node.Con = makeConNodeStub(MnId.Con(3L), IoName("undefinedName"), IoIndex(0))
    val ioVars: IoVars = IoVars[IO](Map(inVarName -> inVar), Map(outVarName -> outVar)).unsafeRunSync()

  "IoVars.apply(in: Map, out: Map)" should:
    "create an IoVars" in newCase[CaseData]: (_, data) =>
      import data.*
      IoVars[IO](Map(inVarName -> inVar), Map(outVarName -> outVar)).asserting: ioVars =>
        ioVars.in mustBe Map(inVarName -> inVar)
        ioVars.out mustBe Map(outVarName -> outVar)

    "raise an error when input and output variable names overlap" in newCase[CaseData]: (tn, data) =>
      import data.*
      val outVarSameName = Variable.Output(inVarName, intType)
      IoVars[IO](Map(inVarName -> inVar), Map(inVarName -> outVarSameName)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(
          "Input and output variable names should not overlap",
        ))

    "raise an error when an input variable's name does not match its map key" in newCase[CaseData]: (tn, data) =>
      import data.*
      IoVars[IO](Map(outVarName -> inVar), Map.empty[IoName, Variable.Output]).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map key should match variable name"))

    "raise an error when an output variable's name does not match its map key" in newCase[CaseData]: (tn, data) =>
      import data.*
      IoVars[IO](Map.empty[IoName, Variable.Input], Map(inVarName -> outVar)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map key should match variable name"))

  "IoVars.apply(in: Set, out: Set)" should:
    "create an IoVars keyed by each variable's own name" in newCase[CaseData]: (_, data) =>
      import data.*
      IoVars[IO](Set(inVar), Set(outVar)).asserting: ioVars =>
        ioVars.in mustBe Map(inVarName -> inVar)
        ioVars.out mustBe Map(outVarName -> outVar)

  "IoVars.conNodesByIo(...)" should:
    "split nodes into input and output sets based on their IoName" in newCase[CaseData]: (tn, data) =>
      import data.*
      ioVars.conNodesByIo[IO](Set(inConNode, outConNode)).logValue(tn).asserting: (inNodes, outNodes) =>
        inNodes mustBe Set(inConNode)
        outNodes mustBe Set(outConNode)

    "raise an error when a node's IoName is not a known input or output variable" in newCase[CaseData]: (tn, data) =>
      import data.*
      ioVars.conNodesByIo[IO](Set(undefinedNode)).logValue(tn).assertThrowsError[AssertionError](_
        .getMessage must include(s"Undefined IO name ${undefinedNode.ioValue.name}"))
