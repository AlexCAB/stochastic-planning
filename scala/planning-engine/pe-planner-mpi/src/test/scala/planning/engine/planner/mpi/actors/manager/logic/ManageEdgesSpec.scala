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

import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.test.actors.ManagerTestActor
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class ManageEdgesSpec extends UnitSpecWithTestKit with ManagerTestActor:
  private class CaseData extends Case with WithManagerActor with MapEdgeTestData

  "ManagerActor.UpsertEdges" should:
    "upsert a single edge" in newCase[CaseData]: (log, data) =>
      import data.*

      val manager = managerActorTwoNode.manager
      val meKey = MeKey.Link(managerActorTwoNode.srcMnId, managerActorTwoNode.trgMnId)

      visualizerProbe.expectMessageType[VisualizerActor.Structure.Nodes.Added] // from managerActorTwoNode setup

      manager ! Actor.UpsertEdges(EdgeData.Kit(Map(meKey -> edgeData1)), adaptorProbe.ref)

      val res = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgesUpserted])
      res.keys mustBe Set(meKey)

      val visMsg = log.msg(visualizerProbe.expectMessageType[VisualizerActor.Structure.Edges.Added])
      visMsg.keys mustBe Set(meKey)

    "upsert multiple edges from a single UpsertEdges message" in newCase[CaseData]: (log, data) =>
      import data.*

      val manager = managerActorTwoNode.manager
      val srcId = managerActorTwoNode.srcMnId
      val trgId = managerActorTwoNode.trgMnId
      val linkKey = MeKey.Link(srcId, trgId)
      val thenKey = MeKey.Then(srcId, trgId)
      val edgeData = EdgeData.Kit(Map(linkKey -> edgeData1, thenKey -> edgeData2))

      visualizerProbe.expectMessageType[VisualizerActor.Structure.Nodes.Added] // from managerActorTwoNode setup

      manager ! Actor.UpsertEdges(edgeData, adaptorProbe.ref)

      val res = log.msg(adaptorProbe.expectMessageType[ManagerAdaptor.EdgesUpserted])
      res.keys mustBe Set(linkKey, thenKey)

      val visMsg = log.msg(visualizerProbe.expectMessageType[VisualizerActor.Structure.Edges.Added])
      visMsg.keys mustBe Set(linkKey, thenKey)
