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

import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.FakeManager
import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
import planning.engine.planner.mpi.test.data.MapNodeTestData

trait WithTestNode:
  self: UnitSpecWithIOAndTestKit =>

  trait WithNode extends MapNodeTestData:
    lazy val fakeManager: FakeManager = FakeManager()
    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer()

    lazy val srcNodeMnId: MnId.Con = MnId.Con(1L)
    lazy val trgNodeMnId: MnId.Abs = MnId.Abs(2L)

    lazy val srcNode: TestNode = TestNode(srcNodeMnId, conNodeData, fakeManager, fakeVisualizer)
    lazy val trgNode: TestNode = TestNode(trgNodeMnId, absNodeData, fakeManager, fakeVisualizer)

    lazy val trgNodeFake: FakeNode = FakeNode(trgNodeMnId, absNodeData.name)
    lazy val srcNodeFake: FakeNode = FakeNode(srcNodeMnId, conNodeData.name)
