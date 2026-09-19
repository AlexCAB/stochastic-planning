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
| created: 2026-09-14 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian

import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.planner.mpi.actors.TestActorBase
import planning.engine.planner.mpi.actors.guardian.logic.{Actor, ApiImpl}

import java.util.concurrent.atomic.AtomicInteger

final case class TestGuardian(api: Guardian):
  import TestGuardian.*

  def ref: ActorRef[Guardian.Msg] = api.ref

object TestGuardian extends TestActorBase:
  private val nameIdCounter: AtomicInteger = AtomicInteger(1)

  private def spawn(bh: Behavior[Guardian.Msg], name: String)(using tk: ActorTestKit): ActorRef[Guardian.Msg] =
    tk.spawn(bh, s"test-guardian-$name-${nameIdCounter.getAndIncrement()}")

  def apply(name: String)(using tk: ActorTestKit): TestGuardian = new TestGuardian(
    api = ApiImpl(spawn(Actor(), name)),
  )

  extension (api: Guardian)
    def ref: ActorRef[Guardian.Msg] = api match
      case ApiImpl(ref) => ref
