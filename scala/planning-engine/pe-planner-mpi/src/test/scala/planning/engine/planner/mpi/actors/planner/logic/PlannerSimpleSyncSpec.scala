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
| created: 2026-10-14 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.logic

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.graph.io.Observation
import planning.engine.common.values.io.{IoIndex, IoTime, IoValue}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.FakeNode
import planning.engine.planner.mpi.actors.planner.TestPlanner
import planning.engine.planner.mpi.actors.planner.data.Message.{Tick, TickDone}
import planning.engine.planner.mpi.actors.planner.data.state.Tick as TickState
import planning.engine.planner.mpi.test.data.NodeTestData

class PlannerSimpleSyncSpec extends UnitSpecWithIOAndTestKit with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    lazy val planner: TestPlanner = TestPlanner(
      "simple-sync-spec",
      inVars = Set(inVar),
      outVars = Set(outVar),
    )

    lazy val inValue1: IoValue = IoValue(inVarName, IoIndex(0))
    lazy val inValue2: IoValue = IoValue(inVarName, IoIndex(1))

    lazy val fakeInNode1: FakeNode = FakeNode(MnId.Con(1L), "in-node-1", Some(inValue1))
    lazy val fakeInNode2: FakeNode = FakeNode(MnId.Con(2L), "in-node-2", Some(inValue2))

    def observation(values: IoValue*): Observation = Observation(IoTime(1L), values.toSet, Some(0.5))

    def sendTick(observation: Observation): Unit =
      val sender = testKit.createTestProbe[TickDone]("tick-sender")
      planner.ref ! Tick(observation, sender.ref)

  "Planner.tick" should:
    "send activation to each concrete node found by observed values" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val obs = observation(inValue1, inValue2)
        planner.api.conNodesAdded[IO](Set(fakeInNode1.conApi, fakeInNode2.conApi)).logValue(tn).await

        sendTick(obs)

        fakeInNode1.expectActivationCon mustBe (obs.time, obs.utility)
        fakeInNode2.expectActivationCon mustBe (obs.time, obs.utility)

    "not send activation to concrete nodes which values are not observed" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val obs = observation(inValue1)
        planner.api.conNodesAdded[IO](Set(fakeInNode1.conApi, fakeInNode2.conApi)).logValue(tn).await

        sendTick(obs)

        fakeInNode1.expectActivationCon mustBe (obs.time, obs.utility)
        fakeInNode2.probe.expectNoMessage()
        succeed

    "move the planner to the next tick with found concrete nodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val obs = observation(inValue1)
        val sender = testKit.createTestProbe[TickDone]("tick-sender")
        val prevNextSteps = planner.getState.tick.nextSteps
        planner.api.conNodesAdded[IO](Set(fakeInNode1.conApi, fakeInNode2.conApi)).logValue(tn).await

        planner.ref ! Tick(obs, sender.ref)
        val tick = planner.getState.tick

        tick mustBe TickState.init.copy(
          time = obs.time,
          sender = Some(sender.ref),
          prevNextSteps = prevNextSteps,
          activeConNodes = Set(fakeInNode1.conApi),
        )
