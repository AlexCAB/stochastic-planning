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
| created: 2026-10-15 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.state.plan

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.{Depth, PathId}
import planning.engine.planner.mpi.actors.node.data.parts.StepNode
import planning.engine.planner.mpi.actors.node.data.parts.Step.{Active, Next, Occurred, Planned}
import planning.engine.planner.mpi.actors.node.data.parts.Struct
import planning.engine.planner.mpi.actors.node.data.state.Plan
import planning.engine.planner.mpi.model.data.node.StepKey

class NodePlanStateSpec extends UnitSpecWithData:
  private class CaseData extends Case:
    val mnId: MnId = MnId.Con(1L)
    val otherMnId: MnId = MnId.Abs(2L)

    val time1: IoTime = IoTime(1L)
    val time2: IoTime = IoTime(2L)

    def stepKey(pathId: Long, depth: Long, id: MnId = mnId): StepKey = StepKey(id, PathId(pathId), Depth(depth))

    val prevKey: StepKey = stepKey(1L, 0L)
    val struct: Struct = Struct.init.copy(prev = Some(prevKey))

    val occurred: Occurred = Occurred(IoTime(0L), Struct.init)
    val nextAt1: Next = Next(time1, struct)
    val nextAt2: Next = Next(time2, Struct.init)
    val planned: Planned = Planned.init(time2)

    val plan: Plan = Plan(
      steps = Map(
        prevKey -> occurred,
        stepKey(1L, 1L) -> nextAt1,
        stepKey(2L, 1L) -> nextAt2,
        stepKey(3L, 2L) -> planned,
      ),
      nextPathId = 4L,
    )

  "Plan.init" should:
    "have no steps and start path IDs from 1" in newCase[CaseData]: (_, _) =>
      async[IO]:
        Plan.init.steps mustBe Map.empty
        Plan.init.nextPathId mustBe 1L
        Plan.init.nextSteps mustBe Map.empty

  "Plan.nextSteps" should:
    "contain only Next steps" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        plan.nextSteps mustBe Map(stepKey(1L, 1L) -> nextAt1, stepKey(2L, 1L) -> nextAt2)

  "Plan.withNewActiveRoot(...)" should:
    "add a new Active root step and increment nextPathId" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val nodeRes = Plan.init.withNewActiveRoot[IO](mnId, time1).logValue(tn).await
        val (node, newPlan) = nodeRes

        node mustBe StepNode(stepKey(1L, 0L), Active.init(time1))
        newPlan.steps mustBe Map(stepKey(1L, 0L) -> Active.init(time1))
        newPlan.nextPathId mustBe 2L

    "use a new path ID for each new root" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val node1Res = Plan.init.withNewActiveRoot[IO](mnId, time1).logValue(tn).await
        val (node1, plan1) = node1Res
        val node2Res = plan1.withNewActiveRoot[IO](mnId, time2).logValue(tn).await
        val (node2, plan2) = node2Res

        node1.key mustBe stepKey(1L, 0L)
        node2.key mustBe stepKey(2L, 0L)
        plan2.steps mustBe Map(stepKey(1L, 0L) -> Active.init(time1), stepKey(2L, 0L) -> Active.init(time2))
        plan2.nextPathId mustBe 3L

    "fail when the plan already contains a step with the new root key" in newCase[CaseData]: (tn, data) =>
      import data.*
      Plan(Map(stepKey(1L, 0L) -> occurred), nextPathId = 1L).withNewActiveRoot[IO](mnId, time1).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Plan already contains step with key"))

  "Plan.withActiveNextSteps(...)" should:
    "activate Next steps with matching time, keeping their structure" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val nodesRes = plan.withActiveNextSteps[IO](mnId, time1).logValue(tn).await
        val (nodes, newPlan) = nodesRes

        nodes mustBe Set(StepNode(stepKey(1L, 1L), Active(time1, struct)))
        newPlan.steps mustBe plan.steps.updated(stepKey(1L, 1L), Active(time1, struct))
        newPlan.nextPathId mustBe plan.nextPathId

    "return no steps and keep the plan unchanged when no Next step matches the time" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val nodesRes = plan.withActiveNextSteps[IO](mnId, IoTime(5L)).logValue(tn).await
        val (nodes, newPlan) = nodesRes

        nodes mustBe Set.empty
        newPlan mustBe plan

    "fail when matched Next step belongs to another node" in newCase[CaseData]: (tn, data) =>
      import data.*
      Plan(
        Map(stepKey(1L, 1L, otherMnId) -> nextAt1),
        nextPathId = 2L,
      ).withActiveNextSteps[IO](mnId, time1).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("StepKey mnId mismatch"))

  "Plan.withActiveSteps(...)" should:
    "activate matching Next steps without adding a new root" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val nodesRes = plan.withActiveSteps[IO](mnId, time2).logValue(tn).await
        val (nodes, newPlan) = nodesRes

        nodes mustBe Set(StepNode(stepKey(2L, 1L), Active.init(time2)))
        newPlan.steps mustBe plan.steps.updated(stepKey(2L, 1L), Active.init(time2))
        newPlan.nextPathId mustBe plan.nextPathId

    "add a new Active root when no Next step matches the time" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val newTime = IoTime(5L)
        val nodesRes = plan.withActiveSteps[IO](mnId, newTime).logValue(tn).await
        val (nodes, newPlan) = nodesRes

        nodes mustBe Set(StepNode(stepKey(4L, 0L), Active.init(newTime)))
        newPlan.steps mustBe plan.steps + (stepKey(4L, 0L) -> Active.init(newTime))
        newPlan.nextPathId mustBe 5L

    "add a new Active root to an empty plan" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val nodesRes = Plan.init.withActiveSteps[IO](mnId, time1).logValue(tn).await
        val (nodes, newPlan) = nodesRes

        nodes mustBe Set(StepNode(stepKey(1L, 0L), Active.init(time1)))
        newPlan mustBe Plan(Map(stepKey(1L, 0L) -> Active.init(time1)), nextPathId = 2L)
