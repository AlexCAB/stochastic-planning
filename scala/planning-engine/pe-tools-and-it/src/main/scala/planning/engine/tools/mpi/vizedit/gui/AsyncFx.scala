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
| created: 2026-10-22 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui

import cats.effect.Async
import cats.effect.implicits.asyncOps
import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import javafx.application.Platform

import scala.concurrent.ExecutionContext

object AsyncFx:
  // Runs tasks on the JavaFX Application Thread.
  private val fxEc: ExecutionContext = ExecutionContext.fromExecutor(r => Platform.runLater(r))

  // Run the whole effect on the FX thread. Since it is single-threaded, steps without async boundaries
  // (e.g. Ref access + FX calls) run without interleaving with other FX work.
  def onFx[F[_]: {Async, Dispatcher}, A](fa: F[A]): F[A] = fa.evalOn(fxEc)

  // Suspend a side effect and run it on the FX thread.
  // Note: must be `delay`, not `blocking` — `blocking` shifts to the CE blocking pool, overriding `evalOn(fxEc)`.
  def fx[F[_]: {Async, Dispatcher}, A](thunk: => A): F[A] = onFx(Async[F].delay(thunk))

  // Fire-and-forget an effect from a JavaFX callback. Must not block: the effect may need the FX thread itself.
  def runF[F[_]: {Async, Dispatcher}](io: F[Unit]): Unit = implicitly[Dispatcher[F]].unsafeRunAndForget(io)
