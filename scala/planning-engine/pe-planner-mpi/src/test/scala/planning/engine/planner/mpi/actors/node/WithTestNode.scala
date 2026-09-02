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

package planning.engine.planner.mpi.actors.node

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.FakeManager
import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
import planning.engine.planner.mpi.actors.planner.FakePlanner
import planning.engine.planner.mpi.common.data.edge.MeRef
import planning.engine.planner.mpi.test.data.{MapEdgeTestData, MapNodeTestData}

trait WithTestNode extends MapNodeTestData with MapEdgeTestData:
  self: UnitSpecWithIOAndTestKit =>

  trait WithNodes extends WithMapNode with WithMapEdge:
    lazy val fakeManager: FakeManager = FakeManager()
    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer()
    lazy val fakePlanner: FakePlanner = FakePlanner()

    lazy val srcNodeMnId: MnId.Con = MnId.Con(1L)
    lazy val trgNodeMnId: MnId.Abs = MnId.Abs(2L)

    lazy val srcNode: TestNode = TestNode(srcNodeMnId, conNodeData, fakeManager, fakeVisualizer, fakePlanner)
    lazy val trgNode: TestNode = TestNode(trgNodeMnId, absNodeData, fakeManager, fakeVisualizer, fakePlanner)

    lazy val trgNodeFake: FakeNode = FakeNode(trgNodeMnId, absNodeData.name, None)
    lazy val srcNodeFake: FakeNode = FakeNode(srcNodeMnId, conNodeData.name, None)

    lazy val linkKey: MeKey = MeKey.Link(srcNodeMnId, trgNodeMnId)
    lazy val thenKey: MeKey = MeKey.Then(trgNodeMnId, srcNodeMnId)

    lazy val meRefSrc: MeRef = MeRef(linkKey, srcNode.api, trgNodeFake.api)
    lazy val meRefTrg: MeRef = MeRef(linkKey, srcNodeFake.api, trgNode.api)
