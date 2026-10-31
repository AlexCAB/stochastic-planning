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
import cats.syntax.all.*
import org.apache.pekko.actor.typed.{Behavior, PostStop}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import planning.engine.planner.mpi.model.error.FatalException
import planning.engine.planner.mpi.repr.Representable

private[actors] trait Stateless extends Base:

  // Shortcut for actor definition
  type Bhv = Behavior[Msg]

  // Actor setup and termination (called once when the actor is created)
  protected def setup[F[_]: S]()(using d: Def, ctx: Ctx): F[Unit] = ().pure
  protected def stopped[F[_]: S]()(using d: Def, ctx: Ctx): F[Unit] = ().pure

  // Abstract method for handling messages
  protected def receive[F[_]: S](msg: Msg)(using Def, Ctx): F[Bhv]

  // Abstract method for handling errors during message processing
  protected def error[F[_]: S](msg: Msg, err: Throwable)(using Def, Ctx): F[Bhv]

  // Common message handler
  protected def doIgnoreError[F[_]: S](msg: Msg, err: Throwable)(using ctx: Ctx): F[Bhv] =
    logError(s"Error processing of the message $msg: ${err.getMessage}", err).as(Behaviors.same)

  protected def doLogAndRaiseFatal[F[_]: S](
      logPrefix: String,
      atMsg: Option[Representable],
      err: Throwable,
      fatalMsg: String,
  )(using Ctx): F[Bhv] =
    for
      msgStr <- renderRepresentable("During processing message:", atMsg)
      logMst = List(Some(logPrefix), msgStr).flatten.mkString("\n")
      _ <- logError(logMst, err)
      _ <- Sync[F].raiseError(FatalException(fatalMsg, Some(err)))
    yield Behaviors.stopped

  // Actor main behavior definition
  protected def behavior(using Def): Bhv = Behaviors.setup: ctx =>
    given Ctx = ctx
    setup[IO]().unsafeRunSync()

    Behaviors
      .receiveMessage[Msg]: msg =>
        handleMsg[IO](msg)(msg => receive[IO](msg), (err, msg) => error[IO](msg, err)).unsafeRunSync()
      .receiveSignal:
        case (ctx, PostStop) => handlePostStop(stopped[IO]()).unsafeRunSync()

  // Factory method for creating the actor's behavior
  private[actors] def apply(definition: Def): Bhv = behavior(using definition)
