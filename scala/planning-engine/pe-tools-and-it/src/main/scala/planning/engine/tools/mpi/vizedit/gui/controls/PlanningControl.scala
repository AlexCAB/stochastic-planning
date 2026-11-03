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
import planning.engine.common.values.io.IoTime
import planning.engine.planner.mpi.model.io.IoVars
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.{NumberField, PlanningVarsTable}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}
import PlanningControl.View

// Control area of the Planning window. Must be constructed on the FX thread (see `PlanningControl.apply`).
class PlanningControl[F[_]: {Async, Dispatcher}](
    inputVarsTable: PlanningVarsTable[F],
    outputVarsTable: PlanningVarsTable[F],
) extends VBox:
  private val viewButtons: List[(View, Button)] = View.values.toList.map: view =>
    view -> new Button(view.label):
      disable = view == View.Context
      onAction = _ => selectView(view)

  private val timeLabel = Label(s"Time = ${IoTime.init.value}")

  private val utilityField = NumberField.float()
  utilityField.prefWidth = 100

  // Selected view button is inactive, all the others are active.
  private def selectView(selected: View): Unit =
    viewButtons.foreach((view, button) => button.disable = view == selected)

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

object PlanningControl:
  enum View(val label: String):
    case Context extends View("Context")
    case ActiveForest extends View("Active forest")
    case PlanningForest extends View("Planning forest")
    case ActionGraph extends View("Action graph")

  def apply[F[_]: {Async, Dispatcher}](): F[PlanningControl[F]] =
    for
      inputVarsTable <- PlanningVarsTable[F](isEditable = true)
      outputVarsTable <- PlanningVarsTable[F](isEditable = false)
      control <- fx(new PlanningControl(inputVarsTable, outputVarsTable))
    yield control
