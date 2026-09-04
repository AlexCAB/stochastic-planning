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
| created: 2026-09-04 |||||||||||*/

package planning.engine.planner.mpi.common.io

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.test.data.MapNodeTestData

class VariableSpec extends UnitSpecWithData with MapNodeTestData:
  private class CaseData extends Case with WithMapNode:
    val outOfRangeNode: Node.Con = makeConNodeStub(MnId.Con(9L), inVarName, IoIndex(1000))

  "Variable.validateNode(...)" should:
    "return the node unchanged when its IoName and index match the variable" in newCase[CaseData]: (_, data) =>
      import data.*
      inVar.validateNode[IO](inConNode).asserting(_ mustBe inConNode)

    "raise an error when the node's IoName does not match the variable's name" in newCase[CaseData]: (tn, data) =>
      import data.*
      inVar.validateNode[IO](outConNode).logValue(tn)
        .assertThrowsError[AssertionError](
          _.getMessage must include(s"Node ${outConNode.mnId} has invalid name for variable $inVarName"),
        )

    "raise an error when the node's index is not defined for the variable's type" in newCase[CaseData]: (tn, data) =>
      import data.*
      inVar.validateNode[IO](outOfRangeNode).logValue(tn)
        .assertThrowsError[AssertionError](
          _.getMessage must include(s"Node ${outOfRangeNode.mnId} has invalid index for variable $inVarName"),
        )