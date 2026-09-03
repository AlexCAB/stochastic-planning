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

package planning.engine.planner.mpi.actors.planner.logic

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.planner.TestPlanner
import planning.engine.planner.mpi.test.data.MapNodeTestData

class PlannerStructureSpec extends UnitSpecWithIOAndTestKit with MapNodeTestData:
  private class CaseData extends Case with WithMapNode:
    lazy val planner: TestPlanner = TestPlanner(
      "structure-spec",
      inputVariables = Map(inVarName -> inVar),
      outputVariables = Map(outVarName -> outVar),
    )

  "Planner.conNodesAdded" should:
    "register a new input node under its IoName and IoIndex" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        planner.api.conNodesAdded[IO](Set(inConNode)).logValue(tn).await

        planner.state.inputNodes mustBe Map(inVarName -> Map(IoIndex(0) -> Set(inConNode)))
        planner.state.outputNodes mustBe Map.empty

    "register a new output node under its mnId" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        planner.api.conNodesAdded[IO](Set(outConNode)).logValue(tn).await

        planner.state.inputNodes mustBe Map.empty
        planner.state.outputNodes mustBe Map(outConNode.mnId -> outConNode)

    "register both input and output nodes from a mixed set in a single call" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        planner.api.conNodesAdded[IO](Set(inConNode, outConNode)).logValue(tn).await

        planner.state.inputNodes mustBe Map(inVarName -> Map(IoIndex(0) -> Set(inConNode)))
        planner.state.outputNodes mustBe Map(outConNode.mnId -> outConNode)

    "accumulate nodes across multiple calls without removing previous ones" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        planner.api.conNodesAdded[IO](Set(inConNode)).logValue(tn).await
        planner.api.conNodesAdded[IO](Set(otherInConNode, outConNode)).logValue(tn).await

        planner.state.inputNodes mustBe Map(
          inVarName -> Map(IoIndex(0) -> Set(inConNode), IoIndex(1) -> Set(otherInConNode)),
        )
        planner.state.outputNodes mustBe Map(outConNode.mnId -> outConNode)
