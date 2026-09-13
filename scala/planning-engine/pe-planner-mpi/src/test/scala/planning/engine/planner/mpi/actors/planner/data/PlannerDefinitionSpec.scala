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

package planning.engine.planner.mpi.actors.planner.data

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.model.io.Variable
import planning.engine.planner.mpi.test.data.MapNodeTestData

class PlannerDefinitionSpec extends UnitSpecWithData with MapNodeTestData:
  private class CaseData extends Case with WithMapNode:
    val undefinedNode: Node.Con = makeConNodeStub(MnId.Con(3L), IoName("undefinedName"), IoIndex(0))
    val definition: Definition = Definition[IO](Map(inVarName -> inVar), Map(outVarName -> outVar)).unsafeRunSync()

  "Definition.apply(inVars: Map, outVars: Map)" should:
    "create a Definition" in newCase[CaseData]: (_, data) =>
      import data.*
      Definition[IO](Map(inVarName -> inVar), Map(outVarName -> outVar)).asserting: definition =>
        definition.inputVariables mustBe Map(inVarName -> inVar)
        definition.outputVariables mustBe Map(outVarName -> outVar)

    "raise an error when input and output variable names overlap" in newCase[CaseData]: (tn, data) =>
      import data.*
      val outVarSameName = Variable.Output(inVarName, intType)
      Definition[IO](Map(inVarName -> inVar), Map(inVarName -> outVarSameName)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Input and output variable names must be unique"))

    "raise an error when an input variable's name does not match its map key" in newCase[CaseData]: (tn, data) =>
      import data.*
      Definition[IO](Map(outVarName -> inVar), Map.empty[IoName, Variable.Output]).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Input variable name mismatch for $outVarName"))

    "raise an error when an output variable's name does not match its map key" in newCase[CaseData]: (tn, data) =>
      import data.*
      Definition[IO](Map.empty[IoName, Variable.Input], Map(inVarName -> outVar)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Output variable name mismatch for $inVarName"))

  "Definition.apply(inVars: Set, outVars: Set)" should:
    "create a Definition keyed by each variable's own name" in newCase[CaseData]: (_, data) =>
      import data.*
      Definition[IO](Set(inVar), Set(outVar)).asserting: definition =>
        definition.inputVariables mustBe Map(inVarName -> inVar)
        definition.outputVariables mustBe Map(outVarName -> outVar)

  "Definition.conNodesByType(...)" should:
    "split nodes into input and output sets based on their IoName" in newCase[CaseData]: (tn, data) =>
      import data.*
      definition.conNodesByType[IO](Set(inConNode, outConNode)).logValue(tn).asserting: (inNodes, outNodes) =>
        inNodes mustBe Set(inConNode)
        outNodes mustBe Set(outConNode)

    "raise an error when a node's IoName is not a known input or output variable" in newCase[CaseData]: (tn, data) =>
      import data.*
      definition.conNodesByType[IO](Set(undefinedNode)).logValue(tn).assertThrowsError[AssertionError](_
        .getMessage must include(s"Undefined IO name ${undefinedNode.ioValue.name}"))
