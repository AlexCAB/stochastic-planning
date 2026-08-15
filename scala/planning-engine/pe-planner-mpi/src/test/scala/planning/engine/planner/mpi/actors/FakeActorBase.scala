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
| created: 14-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors

import org.apache.pekko.actor.FSM.CurrentState
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit

trait FakeActorBase:
  def getActorState[S](using testKit: ActorTestKit): S =
    val probe = testKit.createTestProbe[CurrentState[S]]("GetActorStateProbe")
    val res = probe.expectMessageType[CurrentState[S]]
    probe.stop()
    res.state
