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

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{Label, TableColumn, TableView}

final case class IoVariableRow(name: String, varType: String, range: String):
  val nameProp: StringProperty = StringProperty(name)
  val typeProp: StringProperty = StringProperty(varType)
  val rangeProp: StringProperty = StringProperty(range)

class IoVariablesTable extends TableView[IoVariableRow](ObservableBuffer.empty[IoVariableRow]):
  columnResizePolicy = TableView.ConstrainedResizePolicyFlexLastColumn
  placeholder = Label("No I/O variables")

  columns ++= Seq(
    column("Name", _.nameProp),
    column("Type", _.typeProp),
    column("Range", _.rangeProp),
  )

  // Must be called on the FX thread.
  def setRows(rows: Seq[IoVariableRow]): Unit = items.value.setAll(rows*)

  private def column(header: String, prop: IoVariableRow => StringProperty) = new TableColumn[IoVariableRow, String]:
    text = header
    cellValueFactory = cell => prop(cell.value)
