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

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.planner.mpi.actors.ActorBase.{CurrentState, GetState}

trait TestActorBase:
  def getActorState[S](actorRef: ActorRef[GetState[S]])(using testKit: ActorTestKit): S =
    val probe = testKit.createTestProbe[CurrentState[S]]("GetActorStateProbe")
    actorRef ! GetState(probe.ref)
    val res = probe.expectMessageType[CurrentState[S]]
    probe.stop()
    res.state
