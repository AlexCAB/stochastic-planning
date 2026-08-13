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
import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.TestNode
import planning.engine.planner.mpi.common.data.edge.MeRef
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class NodeStructureSpec extends UnitSpecWithIOAndTestKit with TestNode:
  private class CaseData extends Case with WithNodeActor with MapEdgeTestData:
    lazy val srcMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNode, trgNodeFake.api)
    lazy val trgMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNode)

  "Node.addEdgeSrc" should:
    "add source end of the edge" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        srcNode.addEdgeSrc[IO](srcMeRef, edgeData1).logValue(tn).await
        trgNodeFake.expectAddEdgeTrg mustBe srcMeRef

    "report an error to the manager when message source does not match this actor ID" in newCase[CaseData]:
      (tn, data) =>
        import data.*
        async[IO]:
          val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNodeFake.api)
          srcNode.addEdgeSrc[IO](badMeRef, edgeData1).logValue(tn).await

          val (source, err) = fakeManager.expectReportedError
          source mustBe srcNode
          err.getMessage must include("AddEdgeSrc message source does not match this actor ID")

  "Node.addEdgeTrg" should:
    "add target end of the edge without reporting an error" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        trgNode.addEdgeTrg[IO](trgMeRef).logValue(tn).await

        fakeManager.probe.expectNoMessage()
        succeed

    "report an error to the manager when message target does not match this actor ID" in newCase[CaseData]:
      (tn, data) =>
        import data.*
        async[IO]:
          val badMeRef = MeRef(MeKey.Link(srcNodeMnId, trgNodeMnId), srcNodeFake.api, trgNodeFake.api)
          trgNode.addEdgeTrg[IO](badMeRef).logValue(tn).await

          val (source, err) = fakeManager.expectReportedError
          source mustBe trgNode
          err.getMessage must include("AddEdgeTrg message target does not match this actor ID")
