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

package planning.engine.tools.mpi.vizedit.gui.controls

import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import planning.engine.tools.mpi.vizedit.adaptors.ControlAdaptor
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.{IoVariablesTable, NetworkGraph}
import planning.engine.tools.mpi.vizedit.gui.windows.{Planning, Samples}
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, Priority, VBox}

// Control area of the Main window. Must be constructed on the FX thread (see `MainControl.apply`).
class MainControl[F[_]: {Async, Dispatcher}](
    graph: NetworkGraph[F],
    control: ControlAdaptor[F],
    samples: Samples[F],
    planning: Planning[F],
) extends HBox:

  private val ioVariablesTable = new IoVariablesTable:
    vgrow = Priority.Always

  padding = Insets(10)
  spacing = 10
  children = Seq(
    new VBox:
      spacing = 10
      children = Seq(
        button("Reset Map")(resetMap),
        button("Load Map")(loadMap),
        button("Show Samples")(samples.window.show),
        button("Show Planning")(planning.window.show),
      )
    ,
    new VBox:
      spacing = 5
      hgrow = Priority.Always
      children = Seq(Label("I/O variables"), ioVariablesTable),
  )

  private def resetMap: F[Unit] =
    for
      _ <- List(graph.clear, samples.graph.clear, planning.graph.clear).sequence_
      _ <- fx(ioVariablesTable.setRows(Seq.empty))
    yield ()

  private def loadMap: F[Unit] = Async[F].unit // TODO: Load map from the database

  private def button(label: String)(action: => F[Unit]): Button = new Button(label):
    maxWidth = Double.MaxValue
    onAction = _ => runF(action)

object MainControl:
  def apply[F[_]: {Async, Dispatcher}](
      graph: NetworkGraph[F],
      control: ControlAdaptor[F],
      samples: Samples[F],
      planning: Planning[F],
  ): F[MainControl[F]] = fx(new MainControl(graph, control, samples, planning))
