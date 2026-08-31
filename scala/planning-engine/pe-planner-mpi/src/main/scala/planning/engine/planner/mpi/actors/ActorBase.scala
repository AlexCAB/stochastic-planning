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

import cats.effect.{IO, Sync}
import cats.syntax.all.*
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.planner.mpi.common.error.FatalException
import planning.engine.planner.mpi.common.repr.Representable

private[actors] trait ActorBase extends ActorExecCtx:
  import ActorBase.GetState

  // Shortcut for actor definition
  type Def
  type Msg <: Representable
  type Ctx = ActorContext[Msg]
  type Ref = ActorRef[Msg]

  // Shortcut for actor state type and Sync type (only for internal use)
  protected type St <: Representable
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

  protected def logAndRaiseFatal[F[_]: S](
      logPrefix: String,
      atMsg: Option[Representable],
      state: St,
      err: Throwable,
      fatalMsg: String,
  )(using Ctx): F[St] =
    def renderOp(prefix: String, obj: Option[Representable]): F[Option[String]] = obj
      .map(_.longAutoRepr.map(r => Some(prefix + "\n" + r.map(s => "    " + s.toString).mkString("\n"))))
      .getOrElse(None.pure)

    def buildLogMsg(msgStr: Option[String], stateStr: Option[String]): String =
      List(Some(logPrefix), msgStr, stateStr).flatten.mkString("\n")

    for
      msgStr <- renderOp("During processing message:", atMsg)
      stateStr <- renderOp("Actor state:", Some(state))
      logMst = buildLogMsg(msgStr, stateStr)
      _ <- logError(logMst, err)
      _ <- Sync[F].raiseError(FatalException(fatalMsg, Some(err)))
    yield state

  protected def doGetState[F[_]: S](msg: GetState[St], state: St)(using ctx: Ctx): F[St] =
    for
      _ <- logInfo(s"GetState message received, returning current state: $state")
      _ <- msg.reply(ActorBase.CurrentState(state))
    yield state

  // Helper method for logging messages
  protected def logInfo[F[_]: S](msg: String)(using ctx: Ctx): F[Unit] = delay(ctx.log.info(msg))

  protected def logInfo[F[_]: S, K, V](msg: String, map: Map[K, V])(using ctx: Ctx): F[Unit] =
    val mapRepr = if map.nonEmpty then s"{\n${map.map((k, v) => s"    $k -> $v").mkString("\n")}\n}" else "{}"
    logInfo(s"$msg:\n$mapRepr")

  protected def logError[F[_]: S](msg: String, err: Throwable)(using ctx: Ctx): F[Unit] = delay(ctx.log.error(msg, err))

  // Actor main behavior definition
  private def behavior(state: St)(using Def): Behavior[Msg] = Behaviors.setup: ctx =>
    given Ctx = ctx
    setup(state)

    Behaviors.receiveMessage: msg =>
      lazy val msgName = msg.getClass.getSimpleName

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

  // Messages used for testing purposes to get the current state of the Actor.
  final case class GetState[S](sender: ActorRef[CurrentState[S]]) extends TestCommand[CurrentState[S]]
  final case class CurrentState[S](state: S)
