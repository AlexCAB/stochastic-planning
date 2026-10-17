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
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.data.State
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.node.data.parts.Step.Active
import planning.engine.planner.mpi.actors.node.data.state.Plan
import planning.engine.planner.mpi.actors.node.{FakeNode, TestNode, WithTestNode}
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.model.data.samples.Sample

class ActiveForestSpec extends UnitSpecWithIOAndTestKit with WithTestNode:
  private class CaseData extends Case with WithNodes:
    lazy val trgNode2Fake: FakeNode = FakeNode(MnId.Abs(3L))
    lazy val thenTrgNodeFake: FakeNode = FakeNode(MnId.Abs(4L))
    lazy val conTrgNodeFake: FakeNode = FakeNode(MnId.Con(5L))

    lazy val time: IoTime = IoTime(1L)
    lazy val utility: Option[Double] = Some(0.5)
    lazy val initPu: PU = PU(p = 1.0, u = 0.0)

    lazy val rootKey: StepKey = StepKey.init(srcNodeMnId, 1L)
    lazy val activePlan: Plan = Plan(Map(rootKey -> Active.init(time)), nextPathId = 2L, pu = Some(initPu))

    lazy val absKey: StepKey = StepKey.init(trgNodeMnId, 1L)
    lazy val absActivePlan: Plan = Plan(Map(absKey -> Active.init(time)), nextPathId = 2L, pu = Some(initPu))

    // Path as it comes to the abstract node from the concrete srcNode.
    lazy val inPath: ActivePath = ActivePath.init(srcNodeFake.conApi, Set(rootKey), initPu)
    lazy val activeSIds: Set[SampleId] = props1.keySet

    // Add edge from the given src node to the given fake node and consume UpsertEdgeTrg message, sent to the fake node.
    def addEdge(src: TestNode, trg: FakeNode, key: MeKey, props: Map[SampleId, Sample.Props]): IO[Unit] =
      for
        _ <- src.api.upsertEdgeSrc[IO](MeRef(key, src.api, trg.api), props)
        _ <- IO.blocking(trg.expectUpsertEdgeTrg)
      yield ()

    def addLink(trg: FakeNode): IO[Unit] = addEdge(srcNode, trg, MeKey.Link(srcNodeMnId, trg.api.mnId), props1)

    def addAbsLink(trg: FakeNode, props: Map[SampleId, Sample.Props] = props1): IO[Unit] =
      addEdge(trgNode, trg, MeKey.Link(trgNodeMnId, trg.api.mnId), props)

    // Expected path after the abstract trgNode appended itself to the `inPath`.
    def appendedPath(weight: HuangWeight): IO[ActivePath] =
      inPath.append[IO](trgNode.absApi, Set(absKey), initPu).map(_.withNextWeight(weight))

  "Node.Con.activation(...)" should:
    "add new Active root step and send ActivationAbs to outgoing LINK neighbor" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addLink(trgNodeFake).await
        srcNode.conApi.activation[IO](time, utility).await

        val (msgTime, sampleIds, path) = trgNodeFake.expectActivationAbs
        msgTime mustBe time
        sampleIds mustBe props1.keySet
        path.pathRoot mustBe srcNodeMnId
        path.linkPath mustBe List.empty
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
        addEdge(srcNode, thenTrgNodeFake, MeKey.Then(srcNodeMnId, thenTrgNodeFake.api.mnId), props1).await
        srcNode.conApi.activation[IO](time, utility).await

        val (msgTime, _, _) = trgNodeFake.expectActivationAbs
        thenTrgNodeFake.probe.expectNoMessage()

        msgTime mustBe time

    "add new Active root step and send active path to planner when there are no outgoing LINK edges (root found)" in
      newCase[CaseData]: (_, data) =>
        import data.*
        async[IO]:
          srcNode.conApi.activation[IO](time, utility).await

          val path = fakePlanner.expectAddActivePath
          path mustBe ActivePath.init(srcNode.conApi, Set(rootKey), initPu)

          val State(_, plan) = srcNode.state
          plan mustBe activePlan

    "report an error to the manager when node already has active steps" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addLink(trgNodeFake).await
        srcNode.conApi.activation[IO](time, utility).await
        trgNodeFake.expectActivationAbs

        srcNode.conApi.activation[IO](time, utility).await
        val (source, err) = fakeManager.expectReportedError

        source mustBe srcNode.api
        err.getMessage must include("Active steps must be empty before activation.")

    "report an error to the manager when outgoing LINK neighbor is not abstract node" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addLink(conTrgNodeFake).await
        srcNode.conApi.activation[IO](time, utility).await

        val (source, err) = fakeManager.expectReportedError
        conTrgNodeFake.probe.expectNoMessage()

        source mustBe srcNode.api
        err.getMessage must include("Expected Node.Abs")

  "Node.Abs.activation(...)" should:
    "add Active step, append itself to the path and send ActivationAbs to outgoing LINK neighbor" in
      newCase[CaseData]: (_, data) =>
        import data.*
        async[IO]:
          addAbsLink(trgNode2Fake).await
          trgNode.absApi.activation[IO](time, activeSIds, inPath).await

          val (msgTime, sampleIds, path) = trgNode2Fake.expectActivationAbs
          val expectedWeight = HuangWeight.init.div[IO](1).await.head
          val expectedPath = appendedPath(expectedWeight).await
          fakePlanner.probe.expectNoMessage()

          msgTime mustBe time
          sampleIds mustBe activeSIds
          path mustBe expectedPath
          path.pathRoot mustBe srcNodeMnId
          path.linkPath mustBe List(trgNodeMnId)
          path.activeNodes mustBe Map(srcNodeFake.api -> Set(rootKey), trgNode.api -> Set(absKey))

          val State(_, plan) = trgNode.state
          plan mustBe absActivePlan

    "split path weight between all active outgoing LINK neighbors" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addAbsLink(trgNode2Fake).await
        addAbsLink(thenTrgNodeFake).await
        trgNode.absApi.activation[IO](time, activeSIds, inPath).await

        val (_, _, path1) = trgNode2Fake.expectActivationAbs
        val (_, _, path2) = thenTrgNodeFake.expectActivationAbs
        val expectedWeights = inPath.weight.div[IO](2).await

        Set(path1.weight, path2.weight) mustBe expectedWeights.toSet
        path1.linkPath mustBe List(trgNodeMnId)
        path2.linkPath mustBe List(trgNodeMnId)

    "send ActivationAbs only to outgoing LINK neighbors with active samples" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        addAbsLink(trgNode2Fake, props1).await
        addAbsLink(thenTrgNodeFake, props2).await
        trgNode.absApi.activation[IO](time, activeSIds, inPath).await

        val (_, sampleIds, _) = trgNode2Fake.expectActivationAbs
        thenTrgNodeFake.probe.expectNoMessage()

        sampleIds mustBe activeSIds

    "send active path to planner when there are no active outgoing LINK neighbors (root found)" in
      newCase[CaseData]: (_, data) =>
        import data.*
        async[IO]:
          addAbsLink(trgNode2Fake, props2).await
          trgNode.absApi.activation[IO](time, activeSIds, inPath).await

          val path = fakePlanner.expectAddActivePath
          val expectedPath = appendedPath(inPath.weight).await
          trgNode2Fake.probe.expectNoMessage()

          path mustBe expectedPath

          val State(_, plan) = trgNode.state
          plan mustBe absActivePlan

    "reuse already activated steps on repeated activation" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        trgNode.absApi.activation[IO](time, activeSIds, inPath).await
        trgNode.absApi.activation[IO](time, activeSIds, inPath).await

        val path1 = fakePlanner.expectAddActivePath
        val path2 = fakePlanner.expectAddActivePath

        path1.activeNodes(trgNode.api) mustBe Set(absKey)
        path2 mustBe path1

        val State(_, plan) = trgNode.state
        plan mustBe absActivePlan

    "report an error to the manager when node already exists in the path" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val loopPath = inPath.append[IO](trgNodeFake.absApi, Set(absKey), initPu).await
        trgNode.absApi.activation[IO](time, activeSIds, loopPath).await

        val (source, err) = fakeManager.expectReportedError
        fakePlanner.probe.expectNoMessage()

        source mustBe trgNode.api
        err.getMessage must include(s"Node $trgNodeMnId already exists in the link path")

    "report an error to the manager when active outgoing LINK neighbor is not abstract node" in newCase[CaseData]:
      (_, data) =>
        import data.*
        async[IO]:
          addAbsLink(conTrgNodeFake).await
          trgNode.absApi.activation[IO](time, activeSIds, inPath).await

          val (source, err) = fakeManager.expectReportedError
          conTrgNodeFake.probe.expectNoMessage()

          source mustBe trgNode.api
          err.getMessage must include("Expected Node.Abs")
