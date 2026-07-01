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
| created: 30.06.2026 |||||||||||*/

package planning.engine.planner.mpi.test.actors

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.data.node.StaticActors

trait StaticTestActors:
  self: UnitSpecWithIOAndTestKit =>

  trait WithStaticActors:
    lazy val managerProbe: TestProbe[ManagerActor.Msg] = testKit.createTestProbe[ManagerActor.Msg]()
    lazy val visualizerProbe: TestProbe[VisualizerActor.Msg] = testKit.createTestProbe[VisualizerActor.Msg]()

    lazy val staticActors: StaticActors = StaticActors(managerProbe.ref, visualizerProbe.ref)
