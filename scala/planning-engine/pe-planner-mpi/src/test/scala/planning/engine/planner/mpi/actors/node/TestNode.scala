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

import cats.effect.IO
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.FakeManager
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.data.MapNodeTestData

import java.util.concurrent.atomic.AtomicInteger

trait TestNode:
  self: UnitSpecWithIOAndTestKit =>

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  trait WithNodeActor extends MapNodeTestData:
    lazy val fakeManager: FakeManager = FakeManager()
    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer()

    def makeNodeActor(id: MnId, data: NodeData): Node = Node
      .spawn[IO](
        id,
        data,
        fakeManager.api,
        fakeVisualizer.api,
        (bh, name) => testKit.spawn(bh, s"test-node-$name-${nameIdCounter.getAndIncrement()}"),
      )
      .unsafeRunSync()

    lazy val srcNodeMnId: MnId.Con = MnId.Con(1L)
    lazy val trgNodeMnId: MnId.Abs = MnId.Abs(2L)

    lazy val srcNode: Node = makeNodeActor(srcNodeMnId, conNodeData)
    lazy val trgNode: Node = makeNodeActor(trgNodeMnId, absNodeData)

    lazy val trgNodeFake: FakeNode = FakeNode(trgNodeMnId, absNodeData.name)
    lazy val srcNodeFake: FakeNode = FakeNode(srcNodeMnId, conNodeData.name)
