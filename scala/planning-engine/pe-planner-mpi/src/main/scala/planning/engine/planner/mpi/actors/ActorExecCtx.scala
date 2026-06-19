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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.effect.unsafe.{IORuntime, IORuntimeConfig}
import scala.concurrent.ExecutionContext

// This trait define separate synchronous execution context for each actor, which execute effects in the actor thread,
// to avoid synchronicity issues with the default global execution context.
// So currently IO used only as a wrapper for effects, for handling errors and for better composition,
// but not for parallelism, which is handled by actors themselves.
// WARNING: This IORuntime should not be passes to another actor or used outside the actor.
trait ActorExecCtx:
  protected def name: String

  private val context: ExecutionContext = new ExecutionContext:
    def execute(runnable: Runnable): Unit = runnable.run()
    def reportFailure(cause: Throwable): Unit = cause.printStackTrace()

  private val (scheduler, schedulerShutdown) = IORuntime.createDefaultScheduler()

  given ioRuntime: IORuntime = IORuntime(
    compute = context,
    blocking = context,
    scheduler = scheduler,
    shutdown = schedulerShutdown,
    config = IORuntimeConfig(),
  )
