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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.effect.Sync
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}

import scala.util.{Failure, Success, Try}

trait ActorBase:

  // Shortcut for actor definition, message type and context type
  type Def
  type Msg
  type Ctx = ActorContext[Msg]
  type Ref = ActorRef[Msg]

  // Shortcut for actor state type and behavior type (only for internal use)
  protected type St
  protected type D[F[_]] = Dispatcher[F]
  protected type S[F[_]] = Sync[F]

  // Actor name for logging purposes, to be defined by concrete actors
  protected def name: String

  // Cats-effect helpers
  protected def delay[F[_]: S, R](f: => R): F[R] = Sync[F].delay(f)

  // Abstract methods for handling messages
  protected def receive[F[_]: {S, D}](msg: Msg, state: St)(using Def, Ctx): F[St]

  // Abstract method for handling errors during message processing (i.e., exceptions thrown in the receive method)
  protected def error[F[_]: {S, D}](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St]

  // Message processing helpers
  protected def ignoreError[F[_]: S](msg: Msg, state: St, err: Throwable)(using ctx: Ctx): F[St] =
    logError(s"Error processing message $msg in state $state: ${err.getMessage}", err).as(state)

  // Helper method for logging messages
  protected def logInfo[F[_]: S](msg: String)(using ctx: Ctx): F[Unit] = delay(ctx.log.info(msg))
  protected def logError[F[_]: S](msg: String, err: Throwable)(using ctx: Ctx): F[Unit] = delay(ctx.log.error(msg, err))

  // Actor main behavior definition
  protected def behavior[F[_]: {S, D as dsp}](state: St)(using Def): Behavior[Msg] = Behaviors.setup: ctx =>
    ctx.setLoggerName(name)

    Behaviors.receive: (c, m) =>
      given Ctx = c
      
      def process: F[St] = receive(m, state).handleErrorWith: err =>
        c.log.error(s"Error on message: $m, calling error() handler", err)
        error(m, state, err)

      Try(dsp.unsafeRunSync(receive(m, state).recoverWith(e => error(m, state, e)))) match
        case Success(nextState) => behavior(nextState)

        case Failure(err) =>
          c.log.error(s"Fatal error on message: $m", err)
          Behaviors.stopped

  // Factory `method for creating the actor's behavior
  protected def apply[F[_]: {S, D}](definition: Def, state: St): Behavior[Msg] =
    given Def = definition
    behavior(state)
