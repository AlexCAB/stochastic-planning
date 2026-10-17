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
| created: 2026-10-18 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data.state

import cats.effect.IO
import cats.effect.cps.*
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.{IoIndex, IoTime}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.planner.data.Message.TickDone
import planning.engine.planner.mpi.actors.planner.data.parts.ActiveSteps
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.test.data.NodeTestData

class TickSpec extends UnitSpecWithIOAndTestKit with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    lazy val conNode1: Node.Con = makeConNodeStub(MnId.Con(1L), inVarName, IoIndex(0))
    lazy val conNode2: Node.Con = makeConNodeStub(MnId.Con(2L), inVarName, IoIndex(1))

    lazy val sender1: ActorRef[TickDone] = testKit.createTestProbe[TickDone]("tick-sender-1").ref
    lazy val sender2: ActorRef[TickDone] = testKit.createTestProbe[TickDone]("tick-sender-2").ref

    lazy val prevKey: StepKey = StepKey.init(MnId.Abs(10L), 1L)
    lazy val nextKey: StepKey = StepKey.init(MnId.Abs(11L), 1L)
    lazy val rootKey: StepKey = StepKey.init(MnId.Abs(12L), 1L)

    lazy val pu: PU = PU(p = 1.0, u = 0.0)
    lazy val halves: List[HuangWeight] = HuangWeight.init.div[IO](2).unsafeRunSync()

    def makePath(root: Node.Con, absId: MnId.Abs, weight: HuangWeight): ActivePath =
      ActivePath(root.mnId, List(absId), Map(root -> Set.empty), pu, weight)

    // Tick with all fields filled, to check that `next` resets them.
    lazy val filledTick: Tick = Tick(
      time = IoTime(5L),
      sender = Some(sender1),
      prevNextSteps = Set(prevKey),
      activeConNodes = Set(conNode1),
      activeSteps = ActiveSteps(Set[MnId](conNode1.mnId)),
      nextSteps = Set(nextKey),
      rootSteps = Set(rootKey),
    )

    // Tick at activation phase, waiting for active paths from both concrete nodes.
    lazy val activeTick: Tick = Tick.init.next(IoTime(1L), Set(conNode1, conNode2), sender1)

  "Tick.init" should:
    "have zero time, no sender and empty collections" in newCase[CaseData]: (_, _) =>
      async[IO]:
        Tick.init.time mustBe IoTime(0)
        Tick.init.sender mustBe None
        Tick.init.prevNextSteps mustBe Set.empty
        Tick.init.activeConNodes mustBe Set.empty
        Tick.init.activeSteps mustBe ActiveSteps.empty
        Tick.init.nextSteps mustBe Set.empty
        Tick.init.rootSteps mustBe Set.empty

  "Tick.next(...)" should:
    "start new tick, moving next steps to previous next steps and resetting other fields" in newCase[CaseData]:
      (tn, data) =>
        import data.*
        IO.pure(filledTick.next(IoTime(6L), Set(conNode1, conNode2), sender2)).logValue(tn)
          .asserting(_ mustBe Tick(
            time = IoTime(6L),
            sender = Some(sender2),
            prevNextSteps = Set(nextKey),
            activeConNodes = Set(conNode1, conNode2),
            activeSteps = ActiveSteps(Set[MnId](conNode1.mnId, conNode2.mnId)),
            nextSteps = Set.empty,
            rootSteps = Set.empty,
          ))

    "init active steps with zero weight for each active concrete node" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(Tick.init.next(IoTime(1L), Set(conNode1, conNode2), sender1)).logValue(tn)
        .asserting(_.activeSteps.weightAcc mustBe Map(
          conNode1.mnId -> HuangWeight.zero,
          conNode2.mnId -> HuangWeight.zero,
        ))

    "init empty active steps when no concrete nodes are active" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(filledTick.next(IoTime(6L), Set.empty, sender2)).logValue(tn)
        .asserting(_.activeSteps mustBe ActiveSteps.empty)

  "Tick.withActivePath(...)" should:
    "add path to active steps and return not built when not all weight collected" in newCase[CaseData]:
      (tn, data) =>
        import data.*
        async[IO]:
          val path = makePath(conNode1, MnId.Abs(3L), halves.head)
          val (isBuilt, tick) = activeTick.withActivePath[IO](path).logValue(tn).await

          isBuilt mustBe false
          tick.activeSteps.paths mustBe Set(path)
          tick.activeSteps.weightAcc mustBe Map(conNode1.mnId -> halves.head, conNode2.mnId -> HuangWeight.zero)
          tick.copy(activeSteps = activeTick.activeSteps) mustBe activeTick

    "return not built when only part of concrete nodes collected full weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      activeTick.withActivePath[IO](makePath(conNode1, MnId.Abs(3L), HuangWeight.init)).logValue(tn)
        .asserting((isBuilt, _) => isBuilt mustBe false)

    "return built when all concrete nodes collected full weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val path1 = makePath(conNode1, MnId.Abs(3L), halves.head)
        val path2 = makePath(conNode1, MnId.Abs(4L), halves(1))
        val path3 = makePath(conNode2, MnId.Abs(3L), HuangWeight.init)

        val (isBuilt1, tick1) = activeTick.withActivePath[IO](path1).await
        val (isBuilt2, tick2) = tick1.withActivePath[IO](path2).await
        val (isBuilt3, tick3) = tick2.withActivePath[IO](path3).logValue(tn).await

        isBuilt1 mustBe false
        isBuilt2 mustBe false
        isBuilt3 mustBe true
        tick3.activeSteps.paths mustBe Set(path1, path2, path3)

    "fail if path root is not one of active concrete nodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      Tick.init.next(IoTime(1L), Set(conNode1), sender1)
        .withActivePath[IO](makePath(conNode2, MnId.Abs(3L), HuangWeight.init)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("is not in the weight accumulator"))
