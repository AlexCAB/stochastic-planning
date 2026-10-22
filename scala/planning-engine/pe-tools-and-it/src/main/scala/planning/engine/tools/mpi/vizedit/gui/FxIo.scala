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

object FxIo:
  // Runs tasks on the JavaFX Application Thread.
  private val fxEc: ExecutionContext = ExecutionContext.fromExecutor(r => Platform.runLater(r))
  
  // Suspend a side effect and run it on the FX thread.
  // Note: must be `delay`, not `blocking` — `blocking` shifts to the CE blocking pool, overriding `evalOn(fxEc)`.
  def fx[F[_]: {Async, Dispatcher}, A](thunk: => A): F[A] = Async[F].delay(thunk).evalOn(fxEc)

  // Fire-and-forget an effect from a JavaFX callback. Must not block: the effect may need the FX thread itself.
  def runF[F[_]: {Async, Dispatcher}](io: F[Unit]): Unit = implicitly[Dispatcher[F]].unsafeRunAndForget(io)
  