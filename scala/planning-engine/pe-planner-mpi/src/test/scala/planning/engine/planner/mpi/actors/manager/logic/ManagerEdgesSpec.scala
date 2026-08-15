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
import org.scalatest.Assertion
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.WithTestManager
import planning.engine.planner.mpi.actors.node.TestNode.state
import planning.engine.planner.mpi.actors.node.{Node, WithTestNode}
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class ManagerEdgesSpec extends UnitSpecWithIOAndTestKit with WithTestManager with WithTestNode:
  private class CaseData extends Case with WithManager with MapEdgeTestData:
    def checkNodeState(node: Node, expOutgoing: Map[MnId, (Node, EdgeData)], expIncoming: Map[MnId, Node]): Assertion =
      val (outgoing, incoming) = node.state
      outgoing mustBe expOutgoing
      incoming mustBe expIncoming

  "ManagerActor.UpsertEdges" should:
    "upsert a single edge" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val srcId = managerTwoNode.srcMnId
        val trgId = managerTwoNode.trgMnId
        val meKey = MeKey.Link(srcId, trgId)

        val keys = managerTwoNode.api.upsertEdges[IO](EdgeData.Kit(Map(meKey -> edgeData1))).logValue(tn).await
        keys mustBe Set(meKey)

        fakeVisualizer.expectShowEdgesAdded mustBe keys

        val (nodeRefMap, _, _) = managerTwoNode.state
        checkNodeState(nodeRefMap(srcId), Map(trgId -> (nodeRefMap(trgId), edgeData1)), Map.empty)
        checkNodeState(nodeRefMap(trgId), Map.empty, Map(srcId -> nodeRefMap(srcId)))

    "upsert multiple edges from a single UpsertEdges message" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val srcId = managerTwoNode.srcMnId
        val trgId = managerTwoNode.trgMnId
        
        val linkKey = MeKey.Link(srcId, trgId)
        val thenKey = MeKey.Then(srcId, trgId)
        val edgeData = EdgeData.Kit(Map(linkKey -> edgeData1, thenKey -> edgeData2))

        val keys = managerTwoNode.api.upsertEdges[IO](edgeData).logValue(tn).await
        keys mustBe Set(linkKey, thenKey)

        fakeVisualizer.expectShowEdgesAdded mustBe keys

        // Link and Then both connect srcId -> trgId, so they join into a single outgoing/incoming entry.
        val joinedData = EdgeData(edgeData1.indexies ++ edgeData2.indexies)

        val (nodeRefMap, _, _) = managerTwoNode.state
        checkNodeState(nodeRefMap(srcId), Map(trgId -> (nodeRefMap(trgId), joinedData)), Map.empty)
        checkNodeState(nodeRefMap(trgId), Map.empty, Map(srcId -> nodeRefMap(srcId)))
