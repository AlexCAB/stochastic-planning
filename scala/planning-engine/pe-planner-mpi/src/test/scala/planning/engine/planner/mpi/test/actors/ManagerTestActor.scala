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

package planning.engine.planner.mpi.test.actors

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.manager.logic.Actor
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.data.MapNodeTestData

import java.util.concurrent.atomic.AtomicInteger

trait ManagerTestActor:
  self: UnitSpecWithTestKit =>

  final case class ManagerWithNodes(manager: Actor.Ref, nodes: Map[MnId, Option[HnName]]):
    private lazy val ids = nodes.keys.toList
    def srcMnId: MnId = ids.headOption.getOrElse(fail("No nodes available in ManagerWithNodes"))
    def trgMnId: MnId = ids.drop(1).headOption.getOrElse(fail("Less than two nodes available in ManagerWithNodes"))

  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  trait WithManagerActor extends MapNodeTestData:
    lazy val adaptorProbe: TestProbe[ManagerAdaptor.Msg] = testKit.createTestProbe[ManagerAdaptor.Msg]("adaptor-probe")
    lazy val nodeProbe: TestProbe[NodeActor.Msg] = testKit.createTestProbe[NodeActor.Msg]("node-probe")

    lazy val visualizerProbe: TestProbe[VisualizerActor.Msg] = testKit
      .createTestProbe[VisualizerActor.Msg]("visualizer-probe")

    def makeManagerActor(name: String): Actor.Ref = Actor
      .spawn(
        Actor.Definition(visualizerProbe.ref),
        (bh, n) => testKit.spawn(bh, s"$n-$name-${nameIdCounter.getAndIncrement()}"),
      )

    def addNodes(data: NodeData.Kit, manager: Actor.Ref): ManagerWithNodes =
      val adaptor = testKit.createTestProbe[ManagerAdaptor.Msg]("addNodes-adaptor-probe")
      manager ! Actor.AddNodes(data, adaptor.ref)
      val res = adaptor.expectMessageType[ManagerAdaptor.NodesAdded]
      adaptor.stop()
      ManagerWithNodes(manager, res.ids)

    lazy val managerActorEmpty: ManagerWithNodes = ManagerWithNodes(makeManagerActor("managerActorEmpty"), Map.empty)

    lazy val managerActorOneConNode: ManagerWithNodes =
      addNodes(NodeData(conNodeData), makeManagerActor("managerActorOneConNode"))

    lazy val managerActorTwoNode: ManagerWithNodes =
      addNodes(NodeData(conNodeData, absNodeData), makeManagerActor("managerActorTwoNode"))
