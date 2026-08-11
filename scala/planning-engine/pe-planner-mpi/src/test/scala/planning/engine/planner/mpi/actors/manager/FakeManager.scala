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

package planning.engine.planner.mpi.actors.manager

import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.planner.mpi.actors.manager.logic.ApiImpl

final case class FakeManager(api: Manager, probe: TestProbe[Manager.Msg])

object FakeManager:
  def apply(using testKit: ActorTestKit): FakeManager =
    val probe = testKit.createTestProbe[Manager.Msg]("FakeManagerProbe")
    FakeManager(ApiImpl(probe.ref), probe)
