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
| created: 29.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic.modules

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.{IoIndex, IoValue}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.WithTestManager
import planning.engine.planner.mpi.actors.manager.data.State
import planning.engine.planner.mpi.actors.manager.data.State.SampleData
import planning.engine.planner.mpi.actors.node.TestNode.stateTyped
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.test.data.EdgeTestData

import scala.concurrent.duration.*

class SamplesSpec extends UnitSpecWithIOAndTestKit with WithTestManager with EdgeTestData:
  private class CaseData extends Case with WithManager with WithMapEdge:
    lazy val nim99: MnId.Nim = MnId.Nim(99L)
    lazy val conIoValue: IoValue = IoValue(inVar.name, IoIndex(0))

    // Total samples count set on each node by the SetSampleTotalCount message.
    def totalSamplesCounts(state: State): Map[MnId, Long] = state.nodeRefMap
      .map((mnId, node) => mnId -> node.stateTyped._1._5)

  "Manager.addManSamples(...)" should:
    "create nodes from the given Nim ids, store the sample, and link its edges" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val sample = manSample(MeKey.Link(nim1, nim2))
        logInfo(tn, sample.longAutoStr[IO]).await

        val added = manager.api
          .addManSamples[IO](Set(sample), Map(nim1 -> conNodeData, nim2 -> absNodeData))
          .logValue(tn).await

        val sampleId = added.keySet.head
        added mustBe Map(sampleId -> sample)

        fakeVisualizer.expectShowNodesAdded mustBe Map(conMnId -> conNodeData, absMnId -> absNodeData)
        fakeVisualizer.expectShowEdgesAdded mustBe Set(MeKey.Link(conMnId, absMnId))
        val shownSamples = fakeVisualizer.expectShowSamplesAdded
        fakeVisualizer.probe.expectNoMessage(200.millis)

        fakePlanner.expectConNodeAdded.map(_.mnId) mustBe Set(conMnId)
        fakePlanner.probe.expectNoMessage(200.millis)

        val state = manager.state
        state.nodeRefMap.keySet mustBe Set(conMnId, absMnId)

        shownSamples mustBe Set(Sample(
          id = sampleId,
          props = sample.props,
          info = Some(sample.info),
          edges = Set(MeKey.Link(conMnId, absMnId)),
          nodes = Map(conMnId -> state.nodeRefMap(conMnId).data, absMnId -> state.nodeRefMap(absMnId).data),
        ))

        state.sampleDataMap mustBe Map(sampleId -> SampleData(
          sample.props,
          Some(sample.info),
          Set(state.nodeRefMap(conMnId), state.nodeRefMap(absMnId)),
        ))

        val ((_, _, srcOutgoing, _, _), _) = state.nodeRefMap(conMnId).stateTyped
        srcOutgoing.keySet mustBe Set(absMnId)
        srcOutgoing(absMnId).sampleIds mustBe Set(sampleId)

        val ((_, trgIncoming, _, _, _), _) = state.nodeRefMap(absMnId).stateTyped
        trgIncoming.keySet mustBe Set(conMnId)
        trgIncoming(conMnId).sampleIds mustBe Set(sampleId)

        totalSamplesCounts(state) mustBe Map(conMnId -> 1L, absMnId -> 1L)

    "reuse an existing node found by name instead of creating a new one" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val sample = manSample(MeKey.Link(nim1, nim2))
        logInfo(tn, sample.longAutoStr[IO]).await

        val added = managerOneConNode.api
          .addManSamples[IO](Set(sample), Map(nim1 -> conNodeData, nim2 -> absNodeData))
          .logValue(tn).await

        fakeVisualizer.expectShowNodesAdded mustBe Map(absMnId -> absNodeData)
        fakeVisualizer.expectShowEdgesAdded mustBe Set(MeKey.Link(conMnId, absMnId))
        val shownSamples = fakeVisualizer.expectShowSamplesAdded
        fakeVisualizer.probe.expectNoMessage(200.millis)
        fakePlanner.probe.expectNoMessage(200.millis)

        val state = managerOneConNode.state
        state.nodeRefMap.keySet mustBe Set(conMnId, absMnId)

        shownSamples mustBe Set(Sample(
          id = added.keySet.head,
          props = sample.props,
          info = Some(sample.info),
          edges = Set(MeKey.Link(conMnId, absMnId)),
          nodes = Map(conMnId -> state.nodeRefMap(conMnId).data, absMnId -> state.nodeRefMap(absMnId).data),
        ))
        state.nextMnId mustBe 3L
        totalSamplesCounts(state) mustBe Map(conMnId -> 1L, absMnId -> 1L)

    "add only nodes when samples is empty" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val added = manager.api.addManSamples[IO](Set.empty, Map(nim1 -> conNodeData)).logValue(tn).await
        added mustBe Map.empty

        fakeVisualizer.expectShowNodesAdded mustBe Map(conMnId -> conNodeData)
        fakeVisualizer.probe.expectNoMessage(500.millis)
        fakePlanner.expectConNodeAdded.map(_.mnId) must contain(conMnId)
        fakePlanner.probe.expectNoMessage(200.millis)

        val state = manager.state
        state.nodeRefMap.keySet mustBe Set(conMnId)
        state.sampleDataMap mustBe Map.empty
        totalSamplesCounts(state) mustBe Map(conMnId -> 0L)

    "terminate when a sample edge references a Nim id not present in the nodes map" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val sample = manSample(MeKey.Link(nim99, nim2))
        manager.api.addManSamples[IO](Set(sample), Map(nim2 -> absNodeData)).logValue(tn).attempt.await

        fakeVisualizer.probe.expectTerminated(manager.ref)
        fakePlanner.probe.expectNoMessage(200.millis)
        succeed

    "terminate when a sample edge references a non-Nim MnId" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val sample = manSample(MeKey.Link(conMnId, nim2))
        manager.api.addManSamples[IO](Set(sample), Map(nim2 -> absNodeData)).logValue(tn).attempt.await

        fakeVisualizer.probe.expectTerminated(manager.ref)
        fakePlanner.probe.expectNoMessage(200.millis)
        succeed

  "Manager.addGenSamples(...)" should:
    "create nodes from newNodes, store the sample, and link edges mixing new and existing ids" in
      newCase[CaseData]: (tn, data) =>
        import data.*
        async[IO]:
          val sample = genSample(MeKey.Link(conMnId, nim1))
          logInfo(tn, sample.longAutoStr[IO]).await

          val added = managerOneConNode.api
            .addGenSamples[IO](Set(sample), Map(nim1 -> None))
            .logValue(tn).await

          val sampleId = added.keySet.head
          added mustBe Map(sampleId -> sample)

          fakeVisualizer.expectShowNodesAdded mustBe Map(absMnId -> NodeData(None))
          fakeVisualizer.expectShowEdgesAdded mustBe Set(MeKey.Link(conMnId, absMnId))
          val shownSamples = fakeVisualizer.expectShowSamplesAdded
          fakeVisualizer.probe.expectNoMessage(200.millis)
          fakePlanner.probe.expectNoMessage(200.millis)

          val state = managerOneConNode.state
          state.nodeRefMap.keySet mustBe Set(conMnId, absMnId)

          shownSamples mustBe Set(Sample(
            id = sampleId,
            props = sample.props,
            info = None,
            edges = Set(MeKey.Link(conMnId, absMnId)),
            nodes = Map(conMnId -> state.nodeRefMap(conMnId).data, absMnId -> state.nodeRefMap(absMnId).data),
          ))

          state.sampleDataMap mustBe Map(sampleId -> SampleData(
            sample.props,
            None,
            Set(state.nodeRefMap(conMnId), state.nodeRefMap(absMnId)),
          ))

          val ((_, _, srcOutgoing, _, _), _) = state.nodeRefMap(conMnId).stateTyped
          srcOutgoing.keySet mustBe Set(absMnId)
          srcOutgoing(absMnId).sampleIds mustBe Set(sampleId)

          totalSamplesCounts(state) mustBe Map(conMnId -> 1L, absMnId -> 1L)

    "add only nodes when samples is empty" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val added = manager.api.addGenSamples[IO](Set.empty, Map(nim1 -> Some(conIoValue))).logValue(tn).await
        added mustBe Map.empty

        fakeVisualizer.expectShowNodesAdded mustBe Map(conMnId -> NodeData(Some(conIoValue)))
        fakeVisualizer.probe.expectNoMessage(200.millis)
        fakePlanner.expectConNodeAdded.map(_.mnId) must contain(conMnId)
        fakePlanner.probe.expectNoMessage(200.millis)

        val state = manager.state
        state.nodeRefMap.keySet mustBe Set(conMnId)
        totalSamplesCounts(state) mustBe Map(conMnId -> 0L)

    "terminate when a sample edge references a Nim id not present in newNodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val sample = genSample(MeKey.Link(nim99, nim1))

        manager.api
          .addGenSamples[IO](Set(sample), Map(nim1 -> Some(conIoValue))).logValue(tn)
          .attempt.await

        fakeVisualizer.probe.expectTerminated(manager.ref)
        fakePlanner.probe.expectNoMessage(200.millis)
        succeed

    "terminate when a sample edge references an MnId not present in current state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val sample = genSample(MeKey.Link(MnId.Con(999L), nim1))

        manager.api
          .addGenSamples[IO](Set(sample), Map(nim1 -> Some(conIoValue))).logValue(tn)
          .attempt.await

        fakeVisualizer.probe.expectTerminated(manager.ref)
        fakePlanner.probe.expectNoMessage(200.millis)
        succeed
