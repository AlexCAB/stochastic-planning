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

import cats.effect.IO
import cats.effect.Sync
import cats.syntax.all.*
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import planning.engine.planner.mpi.common.error.FatalException
import planning.engine.planner.mpi.common.repr.Representable

private[actors] trait ActorBase extends ActorExecCtx:
  import ActorBase.GetState

  // Shortcut for actor definition, message type and context type
  type Def
  type Msg
  type Ctx = ActorContext[Msg]
  type Ref = ActorRef[Msg]

  // Shortcut for actor state type and behavior type (only for internal use)
  protected type St
  protected type S[F[_]] = Sync[F]

  // Cats-effect helpers
  protected def delay[F[_]: S, R](f: => R): F[R] = Sync[F].delay(f)

  // Actor setup (called once when the actor is created)
  protected def setup(state: St)(using Def, Ctx): Unit = ()

  // Abstract method for handling messages
  protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St]

  // Abstract method for handling errors during message processing (i.e., exceptions thrown in the receive method)
  protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St]

  // Message processing helpers
  protected def doIgnoreError[F[_]: S](msg: Msg, state: St, err: Throwable)(using ctx: Ctx): F[St] =
    logError(s"Error processing message $msg in state $state: ${err.getMessage}", err).as(state)

  protected def doGetState[F[_]: S](msg: GetState[St], state: St)(using ctx: Ctx): F[St] =
    for
      _ <- logInfo(s"GetState message received, returning current state: $state")
      _ <- msg.reply(ActorBase.CurrentState(state))
    yield state

  // Helper method for logging messages
  protected def logInfo[F[_]: S](msg: String)(using ctx: Ctx): F[Unit] = delay(ctx.log.info(msg))

  protected def logError[F[_]: S](msg: String, err: Throwable)(using ctx: Ctx): F[Unit] = delay(ctx.log.error(msg, err))

  protected def logInfo[F[_]: S, K, V](msg: String, map: Map[K, V])(using ctx: Ctx): F[Unit] =
    logInfo(s"$msg:\n${map.map((k, v) => s"    $k -> $v").mkString("\n")}")

  // Actor main behavior definition
  protected def behavior(state: St)(using Def): Behavior[Msg] = Behaviors.setup: ctx =>
    given Ctx = ctx
    setup(state)

    Behaviors.receiveMessage: msg =>
      val msgName = msg.getClass.getSimpleName

      def recoverableErr(err: Throwable): IO[Behavior[Msg]] =
        ctx.log.error(s"Error on message, calling error() handler, at msg = $msgName", err)

        error[IO](msg, state, err)
          .map(ns => behavior(ns))
          .handleErrorWith: err =>
            ctx.log.error(s"Failed to recover after error, actor will terminated: at msg = $msgName", err)
            IO.delay(Behaviors.stopped)

      def fatalErr(err: FatalException): IO[Behavior[Msg]] =
        ctx.log.error(s"Received FatalException, actor will terminated: at msg = $msgName", err)
        IO.delay(Behaviors.stopped)

      receive[IO](msg, state)
        .map(ns => behavior(ns))
        .handleErrorWith:
          case err: FatalException => fatalErr(err)
          case err: Throwable      => recoverableErr(err)
        .unsafeRunSync()

  // Factory method for creating the actor's behavior
  protected def apply(definition: Def, state: St): Behavior[Msg] =
    given Def = definition
    behavior(state)

private[actors] object ActorBase:

  // Base trait for command messages that require a reply to the sender.
  trait WithSender[R]:
    def sender: ActorRef[R]

    def reply[F[_]: Sync](msg: R): F[Unit] = Sync[F].delay(sender.tell(msg)).void

  // Base trait for messages that used for testing of actors.
  sealed trait TestCommand[R] extends WithSender[R] with Representable
  sealed trait TestResult extends Representable

  // Used for testing purposes to get the current state of the Actor.
  final case class GetState[S](sender: ActorRef[CurrentState[S]]) extends TestCommand[CurrentState[S]]
  final case class CurrentState[S](state: S)
