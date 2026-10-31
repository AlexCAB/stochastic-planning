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

import cats.effect.{Async, Deferred}
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import planning.engine.common.values.sample.SampleId
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{Label, TableColumn, TableRow, TableView}
import SamplesTable.Row
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.tools.mpi.vizedit.gui.controls.SamplesControl

// Table of the map samples. Must be constructed on the FX thread (see `SamplesTable.apply`).
class SamplesTable[F[_]: {Async, Dispatcher}] private (control: Deferred[F, SamplesControl[F]])
    extends TableView[Row](ObservableBuffer.empty[Row]):

  private def column(header: String, prop: Row => StringProperty) = new TableColumn[Row, String]:
    text = header
    cellValueFactory = cell => prop(cell.value)

  private def showSample(id: SampleId): F[Unit] = control.get.flatMap(_.showSample(id))

  columnResizePolicy = TableView.ConstrainedResizePolicyFlexLastColumn
  placeholder = Label("No samples")

  // Item of an empty row (below the last sample) is `null`.
  rowFactory = _ =>
    new TableRow[Row]:
      onMouseClicked = _ => Option(item.value).foreach(row => runF(showSample(row.id)))

  columns ++= Seq(
    column("ID", _.idProp),
    column("Name", _.nameProp),
    column("Params", _.paramsProp),
  )

  private def toRow(sample: Sample): Row = Row(
    id = sample.id,
    name = sample.info.map(_.name.value).getOrElse(""),
    params = s"PN(${sample.props.probabilityCount}), U(${sample.props.utility})",
  )

  def setControl(control: SamplesControl[F]): F[Unit] = this.control.complete(control).void

  // Sorted by sample ID.
  def update(samples: Iterable[Sample]): F[Unit] =
    val rows = samples.toList.sortBy(_.id.value).map(toRow)
    fx(items.value.setAll(rows*))

  def clear: F[Unit] = fx(items.value.clear())

object SamplesTable:
  final case class Row(id: SampleId, name: String, params: String):
    val idProp: StringProperty = StringProperty(id.value.toString)
    val nameProp: StringProperty = StringProperty(name)
    val paramsProp: StringProperty = StringProperty(params)

  def apply[F[_]: {Async, Dispatcher}](): F[SamplesTable[F]] =
    for
        control <- Deferred[F, SamplesControl[F]]
    yield new SamplesTable[F](control)
