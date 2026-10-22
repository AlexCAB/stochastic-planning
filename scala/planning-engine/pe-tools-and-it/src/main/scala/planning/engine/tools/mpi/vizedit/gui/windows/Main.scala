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

package planning.engine.tools.mpi.vizedit.gui.windows

import cats.effect.implicits.monadCancelOps_
import cats.syntax.all.*
import cats.effect.{Async, Deferred, Ref, Resource}
import cats.effect.std.Dispatcher
import planning.engine.tools.mpi.vizedit.gui.FxIo.*
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.stage.Stage

import scala.concurrent.duration.*

class Main[F[_]: {Async, Dispatcher}](counter: Ref[F, Int], closed: Deferred[F, Unit]):
  val label = Label("0")

  val incBtn = new Button("Increment"):
    onAction = _ =>
      runF(counter.updateAndGet(_ + 1).flatMap(n => fx(label.text = n.toString)))

  val slowBtn = new Button("Slow task"):
    onAction = _ =>
      val setBusy = fx:
        disable = true
        label.text = "Working..."

      runF(
        (setBusy *> Async[F].sleep(2.seconds) *> fx(label.text =
          "Done!",
        )) // sleep runs on the CE pool, UI stays responsive
          .guarantee(fx(this.disable = false)), // re-enable even on error/cancel
      )

  val stage = new Stage:
    title = "ScalaFX + cats-effect"
    scene = new Scene(300, 150):
      root = VBox(10, label, incBtn, slowBtn)

    onHidden = _ => runF(closed.complete(()).void)

object Main:
  def apply[F[_]: {Async, Dispatcher}](closed: Deferred[F, Unit]): Resource[F, Main[F]] = Resource.eval:
    for
      counter <- Ref[F].of(0)
      main <- fx(new Main(counter, closed))
      _ <- fx(main.stage.show())
    yield main
