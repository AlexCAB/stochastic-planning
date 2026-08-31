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
| created: 05.07.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic

import cats.effect.IO
import cats.effect.cps.*
import org.scalatest.Assertion
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.data.state.Struct
import planning.engine.planner.mpi.actors.node.{Node, WithTestNode}
import planning.engine.planner.mpi.common.data.edge.MeRef
import planning.engine.planner.mpi.actors.node.data.State

class NodeStructureSpec extends UnitSpecWithIOAndTestKit with WithTestNode:
  private class CaseData extends Case with WithNodes

  "State.upsertEdgeSrc(...)" should:
    "add edge to outgoing map and sample map when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        srcNode.api.upsertEdgeSrc[IO](meRefSrc, props1).await
        trgNodeFake.expectUpsertEdgeTrg mustBe (meRefSrc, props1)

        val State(struct, _) = srcNode.state
        struct.outgoingMap mustBe Map(trgNodeMnId -> Struct.EdgeData(trgNodeFake.api, props1.keySet))
        struct.sampleMap.keySet mustBe props1.keySet
        struct.sampleMap.values.map(_.props).toSet mustBe props1.values.toSet
        struct.sampleMap.values.map(_.index).toSet mustBe Set(1, 2, 3).map(HnIndex(_))
        struct.nextHnIndex mustBe 4L

    "join sample IDs when edge to same target already exists" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        srcNode.api.upsertEdgeSrc[IO](meRefSrc, props1).await
        trgNodeFake.expectUpsertEdgeTrg

        srcNode.api.upsertEdgeSrc[IO](meRefSrc, props2).await
        val allSampleIds = props1.keySet ++ props2.keySet

        trgNodeFake.expectUpsertEdgeTrg mustBe (meRefSrc, props2)

        val State(struct, _) = srcNode.state
        struct.outgoingMap mustBe Map(trgNodeMnId -> Struct.EdgeData(trgNodeFake.api, allSampleIds))
        struct.sampleMap.keySet mustBe allSampleIds
        struct.sampleMap.values.map(_.props).toSet mustBe (props1.values.toSet ++ props2.values.toSet)
        struct.nextHnIndex mustBe 6L

    "report an error to the manager when edge source does not match this actor" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val badMeRef =
          MeRef(linkKey, trgNodeFake.api, trgNodeFake.api) // srcNode field should be srcNode, not trgNodeFake

        srcNode.api.upsertEdgeSrc[IO](badMeRef, props1).await

        val (source, err) = fakeManager.expectReportedError
        source mustBe srcNode.api
        err.getMessage must include("Edge source node does not match this node")

  "State.upsertEdgeTrg(...)" should:
    "add edge to incoming map and sample map when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        trgNode.api.upsertEdgeTrg[IO](meRefTrg, props1).await

        val State(struct, _) = trgNode.state
        struct.incomingMap mustBe Map(srcNodeMnId -> Struct.EdgeData(srcNodeFake.api, props1.keySet))
        struct.sampleMap.keySet mustBe props1.keySet
        struct.sampleMap.values.map(_.props).toSet mustBe props1.values.toSet
        struct.sampleMap.values.map(_.index).toSet mustBe Set(1, 2, 3).map(HnIndex(_))
        struct.nextHnIndex mustBe 4L

    "leave state unchanged when the same edge and samples are upserted again" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        trgNode.api.upsertEdgeTrg[IO](meRefTrg, props1).await
        val filled = trgNode.state

        trgNode.api.upsertEdgeTrg[IO](meRefTrg, props1).await
        trgNode.state mustBe filled

    "report an error to the manager when edge target does not match this actor" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val badMeRef =
          MeRef(linkKey, srcNodeFake.api, srcNodeFake.api) // trgNode field should be trgNode, not srcNodeFake

        trgNode.api.upsertEdgeTrg[IO](badMeRef, props1).await

        val (source, err) = fakeManager.expectReportedError
        source mustBe trgNode.api
        err.getMessage must include("Edge target node does not match this node")
