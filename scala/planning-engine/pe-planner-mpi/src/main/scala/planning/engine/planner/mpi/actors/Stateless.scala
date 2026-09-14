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
| created: 2026-09-13 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.effect.{IO, Sync}
import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.Behaviors

private[actors] trait Stateless extends Base:

  // Shortcut for actor definition
  type Bhv = Behavior[Msg]

  // Actor setup (called once when the actor is created)
  protected def setup()(using Ctx): Unit = ()

  // Abstract method for handling messages
  protected def receive[F[_]: S](msg: Msg)(using Ctx): F[Bhv]

  // Actor main behavior definition
  private def behavior(): Bhv = Behaviors.setup: ctx =>
    given Ctx = ctx
    setup()

    def fatalErr(msg: Msg, err: Throwable): IO[Bhv] =
      ctx.log.error(s"Fatal exception, actor will be terminated: at msg = ${msg.getClass.getSimpleName}", err)
      IO.delay(Behaviors.stopped)

    def handleMsg(msg: Msg): Bhv = receive[IO](msg)
      .handleErrorWith(err => fatalErr(msg, err))
      .unsafeRunSync()

    Behaviors.receiveMessage(handleMsg)

  // Factory method for creating the actor's behavior
  private[actors] def apply(): Bhv = behavior()
