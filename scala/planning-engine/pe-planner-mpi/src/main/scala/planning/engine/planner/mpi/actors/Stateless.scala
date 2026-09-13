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

  // Actor setup (called once when the actor is created)
  protected def setup()(using Ctx): Unit = ()

  // Abstract method for handling messages
  protected def receive[F[_]: S](msg: Msg)(using Ctx): F[Unit]

  // Actor main behavior definition
  private def behavior(): Behavior[Msg] = Behaviors.setup: ctx =>
    given Ctx = ctx
    setup()

    Behaviors.receiveMessage: msg =>
      def fatalErr(err: Throwable): IO[Behavior[Msg]] =
        ctx.log.error(s"Received FatalException, actor will terminated: at msg = ${msg.getClass.getSimpleName}", err)
        IO.delay(Behaviors.stopped)

      receive[IO](msg)
        .map(_ => Behaviors.same)
        .handleErrorWith(err => fatalErr(err))
        .unsafeRunSync()

  // Factory method for creating the actor's behavior
  protected def apply(): Behavior[Msg] = behavior()
