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
    baseTitle: String,
    config: Window.Config,
    graph: NetworkGraph[F],
    control: Node,
    onWindowHidden: F[Unit],
):

  // Position and size are of the whole stage (including window decorations), so `getConfig` returns the same values.
  private val stage: Stage = new Stage:
    title = baseTitle
    x = config.x
    y = config.y
    width = config.width
    height = config.height
    scene = new Scene:
      root = new BorderPane:
        center = graph
        bottom = fixedHeight(control)
    onHidden = _ => runF(onWindowHidden)

  def setTitle(title: Option[String]): F[Unit] = fx(stage.title = baseTitle + title.map(t => s" - $t").getOrElse(""))

  def show: F[Unit] =
    for
      _ <- fx(stage.show())
      _ <- graph.initGraph
    yield ()

  // No-op if the window is already hidden.
  def close: F[Unit] = fx(stage.close())

  def getConfig: F[Window.Config] = fx:
    Window.Config(
      x = stage.x.value,
      y = stage.y.value,
      width = stage.width.value,
      height = stage.height.value,
    )

  private def fixedHeight(content: Node): Node = new BorderPane:
    center = content
    minHeight = Window.controlAreaHeight
    prefHeight = Window.controlAreaHeight
    maxHeight = Window.controlAreaHeight
    maxWidth = Double.MaxValue

object Window:
  final case class Config(x: Double, y: Double, width: Double, height: Double)

  def defaultConf(offset: Double): Config = Config(offset, offset, width = 1024, height = 768)

  val controlAreaHeight: Double = 220

  def apply[F[_]: {Async, Dispatcher}](
      windowTitle: String,
      config: Window.Config,
      graph: NetworkGraph[F],
      control: Node,
      onWindowHidden: F[Unit],
  ): F[Window[F]] = fx(new Window(windowTitle, config, graph, control, onWindowHidden))
