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
import cats.effect.std.{AtomicCell, Dispatcher}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.adaptors.{ControlAdaptor, VisualizerAdaptor}
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.{IoVarsTable, NetworkGraph}
import planning.engine.tools.mpi.vizedit.gui.windows.{Planning, Samples}
import scalafx.Includes.*
import scalafx.geometry.Insets
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.stage.FileChooser

import java.nio.file.{Files, Path}
import MainControl.State

// Control area of the Main window. Must be constructed on the FX thread (see `MainControl.apply`).
class MainControl[F[_]: {Async, Dispatcher, LoggerFactory}](
    graph: NetworkGraph[F],
    ioVariablesTable: IoVarsTable[F],
    control: ControlAdaptor[F],
    visualization: VisualizerAdaptor[F],
    samples: Samples[F],
    planning: Planning[F],
    state: AtomicCell[F, State],
) extends HBox:
  private val logger = LoggerFactory[F].getLogger

  ioVariablesTable.vgrow = Priority.Always

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
      _ <- control.reset()
      _ <- visualization.reset()
    yield ()

  // Standard open file dialog, modal to the Main window; `None` if the user cancels it.
  private def chooseYedFile(workDir: Path): F[Option[Path]] = fx:
    val chooser = new FileChooser:
      title = "Load Map"
      if Files.isDirectory(workDir) then initialDirectory = workDir.toFile // Dialog fails on a non-existing one
      extensionFilters += FileChooser.ExtensionFilter("yEd graph (*.graphml)", "*.graphml")

    Option(chooser.showOpenDialog(scene.value.getWindow)).map(_.toPath)

  // Modal to the Main window, blocks until the user closes it.
  private def showMessage(message: String): F[Unit] = fx:
    new Alert(AlertType.Information):
      initOwner(scene.value.getWindow)
      title = "Load Map"
      headerText = None
      contentText = message
    .showAndWait()
    ()

  def getWorkDir: F[Path] = state.get.map(_.workDir)

  // Work directory is updated to the directory of the loaded file, only if the map was loaded successfully.
  private def chooseAndLoad: F[Unit] = state.evalUpdate: st =>
    chooseYedFile(st.workDir).flatMap:
      case Some(path) => control.loadMap(path).as(st.copy(workDir = path.getParent))
      case None       => logger.info("Load map cancelled by user").as(st)

  private def loadMap: F[Unit] = control.isMapLoaded.ifM(
    showMessage("Map network already loaded, reset the current to load new one"),
    chooseAndLoad,
  )

  private def button(label: String)(action: => F[Unit]): Button = new Button(label):
    maxWidth = Double.MaxValue
    onAction = _ => runF(action)

object MainControl:
  final case class State(workDir: Path)

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](
      workDir: Path,
      graph: NetworkGraph[F],
      ioVariablesTable: IoVarsTable[F],
      control: ControlAdaptor[F],
      visualization: VisualizerAdaptor[F],
      samples: Samples[F],
      planning: Planning[F],
  ): F[MainControl[F]] =
    for
      state <- AtomicCell[F].of(State(workDir))
      control <- fx(new MainControl(graph, ioVariablesTable, control, visualization, samples, planning, state))
    yield control
