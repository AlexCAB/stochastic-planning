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
| created: 2026-10-23 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.components

import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph
import scalafx.Includes.*
import scalafx.scene.layout.BorderPane
import scalafx.scene.{Node, Scene}
import scalafx.stage.Stage

// Window: graph network view in the upper area (resizable in both directions),
// control area at the bottom (fixed height, resizable only horizontally).
// Must be constructed on the FX thread (see `Window.apply`).
class Window[F[_]: {Async, Dispatcher}](
    windowTitle: String,
    graph: NetworkGraph[F],
    control: Node,
    onWindowHidden: F[Unit],
):

  private val stage: Stage = new Stage:
    title = windowTitle
    scene = new Scene(Window.WindowWidth, Window.WindowHeight):
      root = new BorderPane:
        center = graph
        bottom = fixedHeight(control)
    onHidden = _ => runF(onWindowHidden)

  def show: F[Unit] =
    for
      _ <- fx(stage.show())
      _ <- graph.initGraph
    yield ()
  
  private def fixedHeight(content: Node): Node = new BorderPane:
    center = content
    minHeight = Window.ControlAreaHeight
    prefHeight = Window.ControlAreaHeight
    maxHeight = Window.ControlAreaHeight
    maxWidth = Double.MaxValue

object Window:
  val WindowWidth: Double = 1024
  val WindowHeight: Double = 768
  val ControlAreaHeight: Double = 220

  def apply[F[_]: {Async, Dispatcher}](
      windowTitle: String,
      graph: NetworkGraph[F],
      control: Node,
      onWindowHidden: F[Unit],
  ): F[Window[F]] = fx(new Window(windowTitle, graph, control, onWindowHidden))

  def apply[F[_]: {Async, Dispatcher}](windowTitle: String, graph: NetworkGraph[F], control: Node): F[Window[F]] =
    apply(windowTitle, graph, control, Async[F].unit)
