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
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.TestManager
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class ManagerEdgesSpec extends UnitSpecWithIOAndTestKit with TestManager:
  private class CaseData extends Case with WithManager with MapEdgeTestData

  "ManagerActor.UpsertEdges" should:
    "upsert a single edge" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val manager = managerTwoNode.manager
        val meKey = MeKey.Link(managerTwoNode.srcMnId, managerTwoNode.trgMnId)

        val keys = manager.upsertEdges[IO](EdgeData.Kit(Map(meKey -> edgeData1))).logValue(tn).await
        keys mustBe Set(meKey)

        fakeVisualizer.expectShowEdgesAdded mustBe keys

    "upsert multiple edges from a single UpsertEdges message" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val manager = managerTwoNode.manager
        val srcId = managerTwoNode.srcMnId
        val trgId = managerTwoNode.trgMnId
        val linkKey = MeKey.Link(srcId, trgId)
        val thenKey = MeKey.Then(srcId, trgId)
        val edgeData = EdgeData.Kit(Map(linkKey -> edgeData1, thenKey -> edgeData2))

        val keys = manager.upsertEdges[IO](edgeData).logValue(tn).await
        keys mustBe Set(linkKey, thenKey)

        fakeVisualizer.expectShowEdgesAdded mustBe keys
