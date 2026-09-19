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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner

import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.Message.ConNodesAdded
import planning.engine.planner.mpi.actors.planner.logic.ApiImpl

final case class FakePlanner(api: Planner, probe: TestProbe[Planner.Msg]):
  def expectConNodeAdded: Set[Node.Con] = probe.expectMessageType[ConNodesAdded].nodes

object FakePlanner:
  def apply()(using testKit: ActorTestKit): FakePlanner =
    val probe = testKit.createTestProbe[Planner.Msg]("FakePlannerProbe")
    FakePlanner(ApiImpl(probe.ref), probe)
