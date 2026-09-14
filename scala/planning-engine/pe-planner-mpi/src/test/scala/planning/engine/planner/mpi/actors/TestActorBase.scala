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
| created: 14.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.ActorRef
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import planning.engine.planner.mpi.actors.Stateful.{CurrentState, GetState}
import planning.engine.planner.mpi.repr.Representable

trait TestActorBase:
  protected given Logger[IO] = Slf4jLogger.getLoggerFromClass[IO](getClass)

  def getActorState[S](actorRef: ActorRef[GetState[S]])(using tk: ActorTestKit): S =
    val probe = tk.createTestProbe[CurrentState[S]]("GetActorStateProbe")
    actorRef ! GetState(probe.ref)
    val res = probe.expectMessageType[CurrentState[S]]
    probe.stop()
    res.state

  def logObj[S <: Representable](logName: String, obj: S)(using IORuntime): S =
    obj.longAutoStr[IO].flatMap(srt => Logger[IO].info(s"$logName: $srt")).unsafeRunSync()
    obj
