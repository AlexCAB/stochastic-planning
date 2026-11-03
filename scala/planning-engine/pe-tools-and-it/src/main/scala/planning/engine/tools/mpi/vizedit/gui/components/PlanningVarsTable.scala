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
| created: 2026-11-03 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.components

import cats.effect.Async
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoValue
import planning.engine.planner.mpi.model.io.{Type, Variable}
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.scene.Node
import scalafx.scene.control.{ComboBox, Label, TableCell, TableColumn, TableView}
import scalafx.util.StringConverter
import PlanningVarsTable.{Row, unset}

import scala.jdk.CollectionConverters.*

// Table of the variables of one direction with their current values, which can be edited by the user
// if `isEditable` is set. Must be constructed on the FX thread (see `PlanningVarsTable.apply`).
class PlanningVarsTable[F[_]: {Async, Dispatcher}] private (isEditable: Boolean)
    extends TableView[Row](ObservableBuffer.empty[Row]):

  private def column(header: String, prop: Row => StringProperty) = new TableColumn[Row, String]:
    text = header
    cellValueFactory = cell => prop(cell.value)

  private def optionsEditor(row: Row, options: List[String]): Node =
    val items: List[Option[String]] = None :: options.map(Some(_))

    new ComboBox[Option[String]](items):
      maxWidth = Double.MaxValue
      converter = StringConverter.toStringConverter(_.getOrElse(unset))
      value = Option(row.valueProp.value).filter(_.nonEmpty)
      value.onChange(row.valueProp.value = value.value.getOrElse(""))

  private def numberEditor(row: Row, field: NumberField): Node =
    field.maxWidth = Double.MaxValue
    field.promptText = unset
    field.text = row.valueProp.value
    field.text.onChange(row.valueProp.value = field.text.value)
    field

  private def valueEditor(row: Row): Node = row.variable.varType match
    case t: Type.Bool => optionsEditor(row, List(true, false).filter(t.acceptable).map(_.toString))
    case t: Type.Opt  => optionsEditor(row, t.options)
    case _: Type.R    => numberEditor(row, NumberField.float())
    case _: Type.N    => numberEditor(row, NumberField.int())

  // Value is stored in the row, so the editors are owned by the cell and can be recreated when a cell is reused.
  private def editableColumn(header: String) = new TableColumn[Row, Row]:
    text = header
    cellValueFactory = cell => ObjectProperty(cell.value)
    cellFactory = (_: TableColumn[Row, Row]) =>
      new TableCell[Row, Row]:
        item.onChange((_, _, row) => graphic = Option(row).map(valueEditor).orNull)

  columnResizePolicy = TableView.ConstrainedResizePolicyFlexLastColumn
  placeholder = Label("No variables")

  columns ++= Seq(
    column("Name", _.nameProp),
    column("Type", _.typeProp),
    if isEditable then editableColumn("Value") else column("Value", _.valueProp),
  )

  // Sorted by name.
  def update(vars: Iterable[Variable]): F[Unit] =
    val rows = vars.toList.sortBy(_.name.value).map(new Row(_))
    fx(items.value.setAll(rows*))

  private def parse[A](variable: Variable, text: String, value: Option[A]): F[A] = value match
    case Some(v) => v.pure
    case None    => s"Invalid value '$text' of variable ${variable.name.value}".assertionError

  private def toIoValue(variable: Variable, text: String): F[IoValue] =
    val index = variable.varType match
      case t: Type.Bool => t.indexForValue[F](text.toBoolean)
      case t: Type.Opt  => t.indexForValue[F](text)
      case t: Type.R    => parse(variable, text, text.toDoubleOption).flatMap(t.indexForValue[F](_))
      case t: Type.N    => parse(variable, text, text.toLongOption).flatMap(t.indexForValue[F](_))

    index.map(IoValue(variable.name, _))

  def hasValues: F[Boolean] = fx(items.value.asScala.exists(_.valueProp.value.nonEmpty))

  // Values of the variables which are not unset.
  def getValues: F[Set[IoValue]] =
    for
      rows <- fx(items.value.asScala.toList.map(row => row.variable -> row.valueProp.value))
      values <- rows.filter(_._2.nonEmpty).traverse(toIoValue)
    yield values.toSet

  private def toText(row: Row, values: Set[IoValue]): F[String] = values.find(_.name == row.variable.name) match
    case Some(value) => row.variable.varType.valueForIndex[F](value.index).map(_.toString)
    case None        => "".pure

  // Variables missing in `values` become unset.
  def setValues(values: Set[IoValue]): F[Unit] =
    for
      rows <- fx(items.value.asScala.toList)
      texts <- rows.traverse(toText(_, values))
      _ <- fx(rows.zip(texts).foreach((row, text) => row.valueProp.value = text))
    yield ()

  def clear: F[Unit] = fx(items.value.clear())

object PlanningVarsTable:
  val unset: String = "unset"

  // Compared by identity: cells keep their editors for an equal item, so a row of a reloaded map must differ.
  final class Row(val variable: Variable):
    val nameProp: StringProperty = StringProperty(variable.name.value)
    val typeProp: StringProperty = StringProperty(IoVarsTable.typeName(variable.varType))
    val valueProp: StringProperty = StringProperty("") // Empty if the value is unset

  def apply[F[_]: {Async, Dispatcher}](isEditable: Boolean): F[PlanningVarsTable[F]] =
    fx(new PlanningVarsTable[F](isEditable))
