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

import cats.effect.Sync
import cats.syntax.all.*
import cats.effect.unsafe.{IORuntime, IORuntimeConfig}

import scala.concurrent.ExecutionContext
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.planner.mpi.model.error.FatalException
import planning.engine.planner.mpi.repr.Representable

private[actors] trait Base:
  private val context: ExecutionContext = new ExecutionContext:
    def execute(runnable: Runnable): Unit = runnable.run()
    def reportFailure(cause: Throwable): Unit = cause.printStackTrace()

  private val (scheduler, schedulerShutdown) = IORuntime.createDefaultScheduler()

  // This trait define separate synchronous execution context for each actor, which execute effects in the actor thread,
  // to avoid synchronicity issues with the default global execution context.
  // So currently IO used only as a wrapper for effects, for handling errors and for better composition,
  // but not for parallelism, which is handled by actors themselves.
  // WARNING: This IORuntime should not be passes to another actor or used outside the actor.
  given IORuntime = IORuntime(
    compute = context,
    blocking = context,
    scheduler = scheduler,
    shutdown = schedulerShutdown,
    config = IORuntimeConfig(),
  )

  // Shortcut for actor types
  type Def
  type Msg <: Representable
  type Ctx = ActorContext[Msg]
  type Ref = ActorRef[Msg]

  protected type S[F[_]] = Sync[F]

  // Cats-effect helpers
  protected def delay[F[_]: S, R](f: => R): F[R] = Sync[F].delay(f)

  // Helper method for logging messages
  protected def renderRepresentable[F[_]: S](prefix: String, obj: Option[Representable]): F[Option[String]] = obj
    .map(_.longAutoRepr.map(r => Some(prefix + "\n" + r.map(s => "    " + s.toString).mkString("\n"))))
    .getOrElse(None.pure)
  
  protected def logInfo[F[_]: S](msg: String)(using ctx: Ctx): F[Unit] = delay(ctx.log.info(msg))

  protected def logMap[F[_]: S, K, V](msg: String, map: Map[K, V])(using ctx: Ctx): F[Unit] =
    val mapRepr = if map.nonEmpty then s"{\n${map.map((k, v) => s"    $k -> $v").mkString("\n")}\n}" else "{}"
    logInfo(s"$msg:\n$mapRepr")

  protected def logSeq[F[_]: S, K, V](msg: String, map: IterableOnce[V])(using ctx: Ctx): F[Unit] =
    val repr = if map.iterator.nonEmpty then s"[\n${map.iterator.map(v => s"    $v").mkString("\n")}\n]" else "[]"
    logInfo(s"$msg:\n$repr")

  protected def logError[F[_]: S](msg: String, err: Throwable)(using ctx: Ctx): F[Unit] = delay(ctx.log.error(msg, err))

  // Handle message
  protected def handleMsg[F[_]: S](msg: Msg)(
      receive: Msg => F[Behavior[Msg]],
      error: (Throwable, Msg) => F[Behavior[Msg]],
  )(using ctx: Ctx): F[Behavior[Msg]] =
    def recoverableErr(err: Throwable): F[Behavior[Msg]] =
      for
        _ <- logError(s"Error on message, calling error() handler, at msg = ${msg.getClass.getSimpleName}", err)
        bh <- error(err, msg)
      yield bh

    def fatalErr(err: Throwable): F[Behavior[Msg]] =
      for
        msgRpr <- msg.longAutoStr
        logMsg = s"Received FatalException or failed to recover after error, actor will terminated: at msg:\n$msgRpr"
        _ <- logError(logMsg, err)
      yield Behaviors.stopped

    def handleError(err: Throwable): F[Behavior[Msg]] = err match
      case fatal: FatalException => fatalErr(fatal)
      case _                     => recoverableErr(err).handleErrorWith(fatalErr)

    receive(msg)
      .handleErrorWith(handleError)

private[actors] object Base:
  trait WithSender[R]:
    def sender: ActorRef[R]
    def reply[F[_]: Sync](msg: R): F[Unit] = Sync[F].delay(sender.tell(msg)).void
