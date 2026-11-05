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
import planning.engine.common.errors.*
import planning.engine.common.values.io.{IoTime, IoValue}
import planning.engine.planner.mpi.model.io.IoVars
import planning.engine.tools.mpi.vizedit.adaptors.ControlAdaptor
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.{NumberField, PlanningVarsTable}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, Label}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}
import planning.engine.tools.mpi.vizedit.gui.visualization.graphs.PlanningGraphs.View
import PlanningControl.State
import planning.engine.tools.mpi.vizedit.gui.visualization.graphs.PlanningGraphs

// Control area of the Planning window. Must be constructed on the FX thread (see `PlanningControl.apply`).
class PlanningControl[F[_]: {Async, Dispatcher}](
    control: ControlAdaptor[F],
    planningGraphs: PlanningGraphs[F],
    inputVarsTable: PlanningVarsTable[F],
    outputVarsTable: PlanningVarsTable[F],
    state: AtomicCell[F, State],
) extends VBox:
  private val viewButtons: List[(View, Button)] = View.values.toList.map: view =>
    view -> new Button(view.label):
      disable = view == View.initial
      onAction = _ => runF(selectView(view))

  private val timeLabel = Label(s"Time = ${IoTime.init.value}")

  private val utilityField = NumberField.float()
  utilityField.prefWidth = 100

  // The whole tick runs inside `evalUpdate`, so ticks can't overlap. Time is advanced only if the tick succeeds.
  private def runTick: F[Unit] = state.evalUpdate: st =>
    val time = st.time + 1

    for
      _ <- control.tick(IoTime(st.time), this)
      _ <- fx(timeLabel.text = s"Time = $time")
    yield st.copy(time = time)

  // Modal to the Planning window, blocks until the user closes it.
  private def showWarning(message: String): F[Unit] = fx:
    new Alert(AlertType.Warning):
      initOwner(scene.value.getWindow)
      title = "Tick"
      headerText = None
      contentText = message
    .showAndWait()
    ()

  private def tick: F[Unit] = inputVarsTable.hasValues.ifM(
    runTick,
    showWarning("Set at least one input variable value to run a tick"),
  )

  // Selected view button is inactive, all the others are active.
  private def selectView(selected: View): F[Unit] =
    for
      _ <- fx(viewButtons.foreach((view, button) => button.disable = view == selected))
      _ <- planningGraphs.show(selected)
    yield ()

  private def varsColumn(title: String, table: PlanningVarsTable[F]): VBox =
    table.vgrow = Priority.Always

    new VBox:
      spacing = 5
      hgrow = Priority.Always
      children = Seq(Label(title), table)

  padding = Insets(10)
  spacing = 10
  children = Seq(
    new HBox:
      spacing = 10
      children = viewButtons.map(_._2)
    ,
    new HBox:
      spacing = 10
      vgrow = Priority.Always
      children = Seq(varsColumn("Input variables", inputVarsTable), varsColumn("Output variables", outputVarsTable))
    ,
    new HBox:
      spacing = 10
      alignment = Pos.CenterLeft
      children = Seq(
        new Button("Tick"):
          prefWidth = 90
          onAction = _ => runF(tick)
        ,
        timeLabel,
        Label("Utility = "),
        utilityField,
        new Region:
          hgrow = Priority.Always
        ,
        new Button("Reset plan"),
      ),
  )

  def update(vars: IoVars): F[Unit] = inputVarsTable.update(vars.in.values) *> outputVarsTable.update(vars.out.values)

  def reset: F[Unit] = inputVarsTable.clear *> outputVarsTable.clear

  def getInputValues: F[Set[IoValue]] = inputVarsTable.getValues

  def setOutputValues(values: Set[IoValue]): F[Unit] = outputVarsTable.setValues(values)

  // `None` if the utility field is empty.
  def getUtility: F[Option[Double]] = fx(utilityField.text.value).flatMap: text =>
    text.toDoubleOption match
      case Some(utility)        => utility.some.pure
      case None if text.isEmpty => none[Double].pure
      case None                 => s"Invalid utility value '$text'".assertionError

object PlanningControl:
  final case class State(time: Long)

  def apply[F[_]: {Async, Dispatcher}](
      control: ControlAdaptor[F],
      planningGraphs: PlanningGraphs[F],
  ): F[PlanningControl[F]] =
    for
      inputVarsTable <- PlanningVarsTable[F](isEditable = true)
      outputVarsTable <- PlanningVarsTable[F](isEditable = false)
      state <- AtomicCell[F].of(State(time = 0))
      planningControl <- fx(new PlanningControl(control, planningGraphs, inputVarsTable, outputVarsTable, state))
    yield planningControl
