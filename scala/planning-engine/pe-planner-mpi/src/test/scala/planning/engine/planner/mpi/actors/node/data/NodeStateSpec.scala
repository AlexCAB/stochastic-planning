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
| created: 04.07.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.effect.IO
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class NodeStateSpec extends UnitSpecWithIOAndTestKit:
  private class CaseData extends Case with MapEdgeTestData:
    val srcRef: NodeActor.Ref = testKit.createTestProbe[NodeActor.Msg]("srcRef").ref
    val trgRef: NodeActor.Ref = testKit.createTestProbe[NodeActor.Msg]("trgRef").ref

    val srcMnId = MnId.Con(1)
    val trcMnId = MnId.Abs(2)

    val meRef = MeRef(MeKey.Link(srcMnId, trcMnId), srcRef, trgRef)
    val outgoing: Map[MnId, (NodeActor.Ref, EdgeData)] = Map(trcMnId -> (trgRef, edgeData1))
    val incoming: Map[MnId, NodeActor.Ref] = Map(srcMnId -> srcRef)

    lazy val emptyState: NodeActor.State = NodeActor.State.init
    lazy val filledState: NodeActor.State = NodeActor.State(outgoing, incoming)

  "State.addEdgeSrc(...)" should:
    "add edges to outgoing list if this empty" in newCase[CaseData]: (_, data) =>
      import data.*

      emptyState.addEdgeSrc[IO](meRef, edgeData1)
        .asserting(_.outgoing mustBe Map(trcMnId -> (trgRef, edgeData1)))

    "join edge data when edge to same target already exists" in newCase[CaseData]: (_, data) =>
      import data.*

      filledState.addEdgeSrc[IO](meRef, edgeData2)
        .asserting(_.outgoing mustBe Map(trcMnId -> (trgRef, EdgeData(edgeData1.indexies ++ edgeData2.indexies))))

    "fail when target ref does not match existing outgoing entry" in newCase[CaseData]: (_, data) =>
      import data.*
      val meRef = MeRef(MeKey.Link(srcMnId, trcMnId), srcRef, srcRef) // srcRef != trgRef stored in state
      filledState.addEdgeSrc[IO](meRef, edgeData2).assertThrows[AssertionError]

  "State.addEdgeTrg(...)" should:
    "add edge to incoming list" in newCase[CaseData]: (_, data) =>
      import data.*
      emptyState.addEdgeTrg[IO](meRef).asserting(_.incoming mustBe Map(srcMnId -> srcRef))

    "return unchanged state when same edge already exists" in newCase[CaseData]: (_, data) =>
      import data.*
      filledState.addEdgeTrg[IO](meRef).asserting(_ mustBe filledState)

    "fail when incoming ref does not match" in newCase[CaseData]: (_, data) =>
      import data.*
      val meRef = MeRef(MeKey.Link(srcMnId, trcMnId), trgRef, trgRef)
      filledState.addEdgeTrg[IO](meRef).assertThrows[AssertionError]
