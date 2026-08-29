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
| created: 01.07.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.WithTestManager
import planning.engine.planner.mpi.actors.node.TestNode.stateTyped
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class ManagerEdgesSpec extends UnitSpecWithIOAndTestKit with WithTestManager with MapEdgeTestData:
  private class CaseData extends Case with WithManager with WithMapEdge

  "Manager.addEdge(...)" should:
    "add an edge and update the source and target node state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val manager = managerTwoNode.withSample(makePropVals(1))

        val sampleId = manager.firstSampleId
        val srcId = manager.srcMnId
        val trgId = manager.trgMnId
        val meKey = MeKey.Link(srcId, trgId)

        val gotKey = manager.api.addEdge[IO](meKey, Set(sampleId)).logValue(tn).await
        gotKey mustBe meKey

        fakeVisualizer.expectShowEdgesAdded mustBe Set(meKey)

        val nodeMap = manager.state.nodeRefMap

        val (_, _, srcOutgoing, _, _) = nodeMap(srcId).stateTyped
        srcOutgoing.keySet mustBe Set(trgId)
        srcOutgoing(trgId).neighbor mustBe nodeMap(trgId)
        srcOutgoing(trgId).sampleIds mustBe Set(sampleId)

        val (_, trgIncoming, _, _, _) = nodeMap(trgId).stateTyped
        trgIncoming.keySet mustBe Set(srcId)
        trgIncoming(srcId).neighbor mustBe nodeMap(srcId)
        trgIncoming(srcId).sampleIds mustBe Set(sampleId)

    "terminate when the source or target node is not found" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val unknownKey = MeKey.Link(MnId.Con(999L), managerTwoNode.trgMnId)

        managerTwoNode.api.addEdge[IO](unknownKey, Set.empty).logValue(tn).attempt.await
        fakeVisualizer.probe.expectTerminated(managerTwoNode.ref)
        succeed

    "terminate when a sample ID is not found" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val meKey = MeKey.Link(managerTwoNode.srcMnId, managerTwoNode.trgMnId)

        managerTwoNode.api.addEdge[IO](meKey, Set(SampleId(999L))).logValue(tn).attempt.await
        fakeVisualizer.probe.expectTerminated(managerTwoNode.ref)
        succeed
