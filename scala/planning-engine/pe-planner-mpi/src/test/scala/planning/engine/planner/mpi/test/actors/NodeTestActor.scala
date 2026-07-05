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

package planning.engine.planner.mpi.test.actors

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import org.apache.pekko.actor.typed.Behavior
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.data.node.{AbsData, ConData, NodeData, StaticActors}
import planning.engine.planner.mpi.test.data.MapNodeTestData

import java.util.concurrent.atomic.AtomicInteger

trait NodeTestActor:
  self: UnitSpecWithTestKit =>

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  trait WithNodeActor extends MapNodeTestData:
    lazy val managerProbe: TestProbe[ManagerActor.Msg] = testKit.createTestProbe[ManagerActor.Msg]()
    lazy val visualizerProbe: TestProbe[VisualizerActor.Msg] = testKit.createTestProbe[VisualizerActor.Msg]()

    lazy val staticActors: StaticActors = StaticActors(managerProbe.ref, visualizerProbe.ref)

    def makeNodeDefinition(id: MnId, data: NodeData): NodeActor.Definition = (id, data) match
      case (i: MnId.Con, d: ConData) => NodeActor.ConDef(i, d, staticActors)
      case (i: MnId.Abs, d: AbsData) => NodeActor.AbsDef(i, d, staticActors)
      case _                         => fail(s"Invalid combination of id: $id and data: $data for NodeActor definition")

    private def spawn(bh: Behavior[NodeActor.Msg], name: String): NodeActor.Ref =
      testKit.spawn(bh, s"test-node-$name-${nameIdCounter.getAndIncrement()}")

    def makeNodeActor(id: MnId, data: NodeData): NodeActor.Ref = NodeActor
      .spawn(List(makeNodeDefinition(id, data)), spawn)
      .headOption.getOrElse(fail(s"Failed to spawn NodeActor for id: $id, data: $data"))._1

    lazy val srcNodeMnId: MnId.Con = MnId.Con(1L)
    lazy val trgNodeMnId: MnId.Abs = MnId.Abs(2L)

    lazy val srcNode: NodeActor.Ref = makeNodeActor(srcNodeMnId, conNodeData)
    lazy val trgNode: NodeActor.Ref = makeNodeActor(trgNodeMnId, absNodeData)
