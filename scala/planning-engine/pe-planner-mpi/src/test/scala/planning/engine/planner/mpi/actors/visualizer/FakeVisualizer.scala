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
| created: 11.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.planner.mpi.actors.visualizer.logic.ApiImpl

final case class FakeVisualizer(api: Visualizer, probe: TestProbe[Visualizer.Msg])

object FakeVisualizer:
  def apply(using testKit: ActorTestKit): FakeVisualizer =
    val probe = testKit.createTestProbe[Visualizer.Msg]("FakeVisualizerProbe")
    FakeVisualizer(ApiImpl(probe.ref), probe)