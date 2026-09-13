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
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.planner.mpi.model.repr.Representable

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
  given ioRuntime: IORuntime = IORuntime(
    compute = context,
    blocking = context,
    scheduler = scheduler,
    shutdown = schedulerShutdown,
    config = IORuntimeConfig(),
  )

  // Shortcut for actor definition
  type Msg <: Representable
  type Ctx = ActorContext[Msg]
  type Ref = ActorRef[Msg]

  protected type S[F[_]] = Sync[F]

  // Cats-effect helpers
  protected def delay[F[_]: S, R](f: => R): F[R] = Sync[F].delay(f)

  // Helper method for logging messages
  protected def logInfo[F[_]: S](msg: String)(using ctx: Ctx): F[Unit] = delay(ctx.log.info(msg))

  protected def logMap[F[_]: S, K, V](msg: String, map: Map[K, V])(using ctx: Ctx): F[Unit] =
    val mapRepr = if map.nonEmpty then s"{\n${map.map((k, v) => s"    $k -> $v").mkString("\n")}\n}" else "{}"
    logInfo(s"$msg:\n$mapRepr")

  protected def logSeq[F[_]: S, K, V](msg: String, map: IterableOnce[V])(using ctx: Ctx): F[Unit] =
    val repr = if map.iterator.nonEmpty then s"[\n${map.iterator.map(v => s"    $v").mkString("\n")}\n]" else "[]"
    logInfo(s"$msg:\n$repr")

  protected def logError[F[_]: S](msg: String, err: Throwable)(using ctx: Ctx): F[Unit] = delay(ctx.log.error(msg, err))

private[actors] object Base:
  trait WithSender[R]:
    def sender: ActorRef[R]
    def reply[F[_]: Sync](msg: R): F[Unit] = Sync[F].delay(sender.tell(msg)).void
