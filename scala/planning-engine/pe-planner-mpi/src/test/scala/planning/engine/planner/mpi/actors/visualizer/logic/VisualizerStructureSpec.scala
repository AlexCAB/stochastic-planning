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
| created: 14.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic

import cats.effect.IO
import org.mockito.Mockito.{timeout, verify}
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.visualizer.{TestVisualizer, WithTestVisualizer}

class VisualizerStructureSpec extends UnitSpecWithIOAndTestKit with WithTestVisualizer with AsyncIdiomaticMockito:
  private class CaseData extends Case with WithVisualizer:
    val conId: MnId.Con = MnId.Con(1L)
    val absId: MnId.Abs = MnId.Abs(2L)

    val conName: Option[HnName] = Some(HnName("Test Con Node"))
    val absName: Option[HnName] = None

    val linkKey: Link = Link(conId, absId)
    val thenKey: Then = Then(absId, conId)

    val ids: Map[MnId, Option[HnName]] = Map(conId -> conName, absId -> absName)
    val keys: Set[MeKey] = Set(linkKey, thenKey)

    val visualizationMock: Visualization = mock[Visualization]
    visualizationMock.init[IO](testMetadata, testVars) returns IO.unit // Called on actor setup, so stub before spawn

    val visualizer: TestVisualizer = makeVisualizer(viz = visualizationMock)

    val callTimeoutMs = 3000L // Visualization is called asynchronously, so verification waits for it.

  "Visualizer.init(...)" should:
    "initialize the visualization with map metadata and variables" in newCase[CaseData]: (_, data) =>
      import data.*
      IO(verify(visualizationMock, timeout(callTimeoutMs)).init[IO](testMetadata, testVars)).asserting(_ => succeed)

  "Visualizer.nodesAdded(...)" should:
    "pass the added nodes to the visualization" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizationMock.nodesAdded[IO](ids) returns IO.unit

      visualizer.api.nodesAdded[IO](ids).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).nodesAdded[IO](ids)
        succeed

  "Visualizer.edgesAdded(...)" should:
    "pass the added edge keys to the visualization" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizationMock.edgesAdded[IO](keys) returns IO.unit

      visualizer.api.edgesAdded[IO](keys).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).edgesAdded[IO](keys)
        succeed
