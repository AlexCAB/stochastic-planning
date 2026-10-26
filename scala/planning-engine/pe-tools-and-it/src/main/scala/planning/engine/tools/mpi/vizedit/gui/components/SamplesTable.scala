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
| created: 2026-10-26 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.components

import cats.effect.Async
import cats.effect.std.Dispatcher
import planning.engine.planner.mpi.model.data.visualization.SampleViz
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{Label, TableColumn, TableView}
import SamplesTable.Row

// Table of the map samples. Must be constructed on the FX thread (see `SamplesTable.apply`).
class SamplesTable[F[_]: {Async, Dispatcher}] private ()
    extends TableView[Row](ObservableBuffer.empty[Row]):

  private def column(header: String, prop: Row => StringProperty) = new TableColumn[Row, String]:
    text = header
    cellValueFactory = cell => prop(cell.value)

  columnResizePolicy = TableView.ConstrainedResizePolicyFlexLastColumn
  placeholder = Label("No samples")

  columns ++= Seq(
    column("ID", _.idProp),
    column("Name", _.nameProp),
    column("Params", _.paramsProp),
  )

  private def toRow(sample: SampleViz): Row = Row(
    id = sample.id.value.toString,
    name = sample.name.map(_.value).getOrElse(""),
    params = s"PN(${sample.probabilityCount}), U(${sample.utility})",
  )

  // Sorted by sample ID.
  def update(samples: Iterable[SampleViz]): F[Unit] =
    val rows = samples.toList.sortBy(_.id.value).map(toRow)
    fx(items.value.setAll(rows*))

  def clear: F[Unit] = fx(items.value.clear())

object SamplesTable:
  final case class Row(id: String, name: String, params: String):
    val idProp: StringProperty = StringProperty(id)
    val nameProp: StringProperty = StringProperty(name)
    val paramsProp: StringProperty = StringProperty(params)

  def apply[F[_]: {Async, Dispatcher}](): F[SamplesTable[F]] = fx(new SamplesTable[F]())
