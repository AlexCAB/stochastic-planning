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
import planning.engine.planner.mpi.model.io.{IoVars, Type, Variable}
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{Label, TableColumn, TableView}
import IoVarsTable.Row

// Table of the map I/O variables. Must be constructed on the FX thread (see `IoVariablesTable.apply`).
class IoVarsTable[F[_]: {Async, Dispatcher}] private ()
    extends TableView[Row](ObservableBuffer.empty[Row]):

  private def column(header: String, prop: Row => StringProperty) = new TableColumn[Row, String]:
    text = header
    cellValueFactory = cell => prop(cell.value)

  columnResizePolicy = TableView.ConstrainedResizePolicyFlexLastColumn
  placeholder = Label("No I/O variables")

  columns ++= Seq(
    column("Name", _.nameProp),
    column("Type", _.typeProp),
    column("Range", _.rangeProp),
  )

  private def toRow(isInput: Boolean)(variable: Variable): Row =
    val typeName = variable.varType match
      case _: Type.N    => "int"
      case _: Type.R    => "float"
      case _: Type.Bool => "bool"
      case _: Type.Opt  => "list"

    Row(variable.name.value, s"${if isInput then "input" else "output"} $typeName", variable.varType.toString)

  // Inputs first, then outputs, each sorted by name.
  def update(vars: IoVars): F[Unit] =
    val rows = vars.in.values.toList.sortBy(_.name.value).map(toRow(isInput = true)) ++
      vars.out.values.toList.sortBy(_.name.value).map(toRow(isInput = false))

    fx(items.value.setAll(rows*))

  def clear: F[Unit] = fx(items.value.clear())

object IoVarsTable:
  final case class Row(name: String, varType: String, range: String):
    val nameProp: StringProperty = StringProperty(name)
    val typeProp: StringProperty = StringProperty(varType)
    val rangeProp: StringProperty = StringProperty(range)

  def apply[F[_]: {Async, Dispatcher}](): F[IoVarsTable[F]] = fx(new IoVarsTable[F]())
