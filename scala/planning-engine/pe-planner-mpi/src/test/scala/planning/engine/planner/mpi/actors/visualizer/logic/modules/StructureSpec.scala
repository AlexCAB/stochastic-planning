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

package planning.engine.planner.mpi.actors.visualizer.logic.modules

import cats.effect.IO
import org.mockito.Mockito.{timeout, verify}
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.visualizer.{TestVisualizer, WithTestVisualizer}
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.data.visualization.SampleViz

class StructureSpec extends UnitSpecWithIOAndTestKit with WithTestVisualizer with AsyncIdiomaticMockito:
  private class CaseData extends Case with WithVisualizer:
    val conId: MnId.Con = MnId.Con(1L)
    val absId: MnId.Abs = MnId.Abs(2L)

    val conData: NodeData = NodeData.Con(Some(HnName("Test Con Node")), None, IoName("testIo"), IoIndex(1L))
    val absData: NodeData = NodeData.Abs(None, None)

    val linkKey: Link = Link(conId, absId)
    val thenKey: Then = Then(absId, conId)

    val ids: Map[MnId, NodeData] = Map(conId -> conData, absId -> absData)
    val keys: Set[MeKey] = Set(linkKey, thenKey)

    def makeNode(id: MnId, name: Option[HnName]): Node =
      val node = mock[Node]
      node.mnId returns id
      node.name returns name
      node

    val conNode: Node = makeNode(conId, conData.name)
    val absNode: Node = makeNode(absId, absData.name)

    val sample: Sample = Sample(
      id = SampleId(1L),
      props = Sample.Props(probabilityCount = 10L, utility = 0.5),
      info = Some(Sample.Info(Name("test-sample"), None)),
      edges = keys,
      nodes = Map(conId -> conNode, absId -> absNode),
    )

    val sampleViz: SampleViz = SampleViz(
      id = SampleId(1L),
      name = Some(Name("test-sample")),
      probabilityCount = 10L,
      utility = 0.5,
      nodes = Set((conId, conData.name, conData.description), (absId, absData.name, absData.description)),
      edges = Set((conId, absId, EdgeType.LINK), (absId, conId, EdgeType.THEN)),
    )

    // All stubbing must be done before the actor spawned: the actor calls the mock from its own thread
    // (starting with `init` on setup), which races with Mockito stubbing done from the test thread.
    val visualizationMock: Visualization = mock[Visualization]
    visualizationMock.init[IO](testMetadata, testVars) returns IO.unit
    visualizationMock.nodesAdded[IO](ids) returns IO.unit
    visualizationMock.edgesAdded[IO](keys) returns IO.unit
    visualizationMock.samplesAdded[IO](Set(sampleViz)) returns IO.unit

    val visualizer: TestVisualizer = makeVisualizer(viz = visualizationMock)

    val callTimeoutMs = 3000L // Visualization is called asynchronously, so verification waits for it.

  "Visualizer.init(...)" should:
    "initialize the visualization with map metadata and variables" in newCase[CaseData]: (_, data) =>
      import data.*
      IO(verify(visualizationMock, timeout(callTimeoutMs)).init[IO](testMetadata, testVars)).asserting(_ => succeed)

  "Visualizer.nodesAdded(...)" should:
    "pass the added nodes to the visualization" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizer.api.nodesAdded[IO](ids).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).nodesAdded[IO](ids)
        succeed

  "Visualizer.edgesAdded(...)" should:
    "pass the added edge keys to the visualization" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizer.api.edgesAdded[IO](keys).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).edgesAdded[IO](keys)
        succeed

  "Visualizer.samplesAdded(...)" should:
    "convert the added samples to SampleViz and pass them to the visualization" in newCase[CaseData]: (tn, data) =>
      import data.*
      visualizer.api.samplesAdded[IO](Set(sample)).logValue(tn).asserting: _ =>
        verify(visualizationMock, timeout(callTimeoutMs)).samplesAdded[IO](Set(sampleViz))
        succeed
