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
| created: 2026-10-16 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic.modules.plan

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.data.State
import planning.engine.planner.mpi.actors.node.data.parts.Step.Active
import planning.engine.planner.mpi.actors.node.data.state.Plan
import planning.engine.planner.mpi.actors.node.{FakeNode, WithTestNode}
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.node.StepKey

class ActiveForestSpec extends UnitSpecWithIOAndTestKit with WithTestNode:
  private class CaseData extends Case with WithNodes:
    lazy val trgNode2Fake: FakeNode = FakeNode(MnId.Abs(3L))
    lazy val thenTrgNodeFake: FakeNode = FakeNode(MnId.Abs(4L))
    lazy val conTrgNodeFake: FakeNode = FakeNode(MnId.Con(5L))

    lazy val time: IoTime = IoTime(1L)
    lazy val utility: Option[Double] = Some(0.5)

    lazy val rootKey: StepKey = StepKey.init(srcNodeMnId, 1L)
    lazy val activePlan: Plan = Plan(Map(rootKey -> Active.init(time)), nextPathId = 2L)
    lazy val initPu: PU = PU(p = 1.0, u = 0.0)

    // Add edge from srcNode to the given fake node and consume UpsertEdgeTrg message, sent to the fake node.
    def addEdge(trg: FakeNode, key: MeKey): IO[Unit] =
      for
        _ <- srcNode.api.upsertEdgeSrc[IO](MeRef(key, srcNode.api, trg.api), props1)
        _ <- IO.blocking(trg.expectUpsertEdgeTrg)
      yield ()

    def addLink(trg: FakeNode): IO[Unit] = addEdge(trg, MeKey.Link(srcNodeMnId, trg.api.mnId))

  "Node.Con.activation(...)" should:
    "add new Active root step and send ActivationAbs to outgoing abstract LINK neighbor" in newCase[CaseData]:
      (_, data) =>
        import data.*
        async[IO]:
          addLink(trgNodeFake).await
          srcNode.conApi.activation[IO](time, utility).await

          val (msgTime, sampleIds, path) = trgNodeFake.expectActivationAbs
          msgTime mustBe time
          sampleIds mustBe props1.keySet
          path.linkPath mustBe List(srcNodeMnId)
          path.activeNodes mustBe Map(srcNode.api -> Set(rootKey))
          path.pu mustBe initPu
          path.weight mustBe HuangWeight.init

          val State(_, plan) = srcNode.state
          plan mustBe activePlan

    "split Huang weight between all outgoing abstract LINK neighbors" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addLink(trgNodeFake).await
        addLink(trgNode2Fake).await
        srcNode.conApi.activation[IO](time, utility).await

        val (_, _, path1) = trgNodeFake.expectActivationAbs
        val (_, _, path2) = trgNode2Fake.expectActivationAbs
        val expectedWeights = HuangWeight.init.div[IO](2).await

        Set(path1.weight, path2.weight) mustBe expectedWeights.toSet
        path1.activeNodes mustBe Map(srcNode.api -> Set(rootKey))
        path2.activeNodes mustBe Map(srcNode.api -> Set(rootKey))

    "not send ActivationAbs to outgoing THEN neighbor" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addLink(trgNodeFake).await
        addEdge(thenTrgNodeFake, MeKey.Then(srcNodeMnId, thenTrgNodeFake.api.mnId)).await
        srcNode.conApi.activation[IO](time, utility).await

        val (msgTime, _, _) = trgNodeFake.expectActivationAbs
        thenTrgNodeFake.probe.expectNoMessage()

        msgTime mustBe time

    "add new Active root step when there are no outgoing LINK edges" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        srcNode.conApi.activation[IO](time, utility).await

        val State(_, plan) = srcNode.state
        plan mustBe activePlan

    "report an error to the manager when outgoing LINK neighbor is not abstract node" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addLink(conTrgNodeFake).await
        srcNode.conApi.activation[IO](time, utility).await

        val (source, err) = fakeManager.expectReportedError
        conTrgNodeFake.probe.expectNoMessage()

        source mustBe srcNode.api
        err.getMessage must include("Expected MnId.Abs and  Node.Abs")
