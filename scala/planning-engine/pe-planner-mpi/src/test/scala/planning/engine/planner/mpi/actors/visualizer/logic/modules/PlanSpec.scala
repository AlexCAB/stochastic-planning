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
| created: 2026-10-20 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic.modules

import cats.effect.IO
import org.mockito.Mockito.{timeout, verify}
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.graph.edges.MeKey.Link
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.{IoIndex, IoName, IoTime}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath.ActiveNode
import planning.engine.planner.mpi.actors.planner.data.parts.ActiveSteps
import planning.engine.planner.mpi.actors.visualizer.{TestVisualizer, WithTestVisualizer}
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.visualization.ActiveForest

class PlanSpec extends UnitSpecWithIOAndTestKit with WithTestVisualizer with AsyncIdiomaticMockito:
  private class CaseData extends Case with WithVisualizer:
    val time: IoTime = IoTime(7L)

    val root1Id: MnId.Con = MnId.Con(1L)
    val root2Id: MnId.Con = MnId.Con(2L)
    val absId1: MnId.Abs = MnId.Abs(3L)
    val absId2: MnId.Abs = MnId.Abs(4L)

    val conPu: PU = PU(p = 1.0, u = 0.0)
    val absPu: PU = PU(p = 0.5, u = 2.0)

    val conData: NodeData.Con = NodeData.Con(Some(HnName("Test Con Node")), None, IoName("testIo"), IoIndex(1L))
    val absData: NodeData.Abs = NodeData.Abs(None, None)

    def mockConNode: Node.Con =
      val node = mock[Node.Con]
      node.data returns conData
      node

    def mockAbsNode: Node.Abs =
      val node = mock[Node.Abs]
      node.data returns absData
      node

    def makePath(rootId: MnId.Con, absIds: List[MnId.Abs], weight: HuangWeight): ActivePath = ActivePath(
      root = rootId,
      path = absIds,
      activeNodes = absIds.map(id => (id: MnId) -> ActiveNode(mockAbsNode, absPu, Set.empty)).toMap
        + (rootId -> ActiveNode(mockConNode, conPu, Set.empty)),
      weight = weight,
    )

    // Root 1 have path 1 -> 3 -> 4, root 2 have path 2 -> 3.
    val steps: ActiveSteps = ActiveSteps(
      weightAcc = Map(root1Id -> HuangWeight.init, root2Id -> HuangWeight.init),
      paths = Set(
        makePath(root1Id, List(absId2, absId1), HuangWeight.init),
        makePath(root2Id, List(absId1), HuangWeight.init),
      ),
    )

    val expectedForest: ActiveForest = ActiveForest(
      nodes = Map(
        root1Id -> ActiveForest.Node(conData, conPu),
        root2Id -> ActiveForest.Node(conData, conPu),
        absId1 -> ActiveForest.Node(absData, absPu),
        absId2 -> ActiveForest.Node(absData, absPu),
      ),
      roots = Set(root1Id, root2Id),
      edges = Set(Link(root1Id, absId1), Link(absId1, absId2), Link(root2Id, absId1)),
    )

    val emptyForest: ActiveForest = ActiveForest(Map.empty, Set.empty, Set.empty)

    // All stubbing must be done before the actor spawned: the actor calls the mock from its own thread
    // (starting with `init` on setup), which races with Mockito stubbing done from the test thread.
    val visualizationMock: Visualization = mock[Visualization]
    visualizationMock.init[IO](testMetadata, testVars) returns IO.unit
    visualizationMock.activeStepsFound[IO](time, expectedForest) returns IO.unit
    visualizationMock.activeStepsFound[IO](time, emptyForest) returns IO.unit

    val visualizer: TestVisualizer = makeVisualizer(viz = visualizationMock)

    val callTimeoutMs = 3000L // Visualization is called asynchronously, so verification waits for it.

  "Visualizer.activeStepsFound(...)" should:
    "convert active steps to active forest and pass it to the visualization" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizer.api.activeStepsFound[IO](time, steps).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).activeStepsFound[IO](time, expectedForest)
        succeed

    "pass empty active forest to the visualization when no active steps found" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizer.api.activeStepsFound[IO](time, ActiveSteps.empty).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).activeStepsFound[IO](time, emptyForest)
        succeed
