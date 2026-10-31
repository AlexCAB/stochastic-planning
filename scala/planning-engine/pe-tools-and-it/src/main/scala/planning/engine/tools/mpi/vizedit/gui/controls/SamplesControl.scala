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
import planning.engine.common.values.sample.SampleId
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.SamplesTable
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, Priority, VBox}
import SamplesControl.State
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.tools.mpi.vizedit.gui.visualization.SampleGraph

// Control area of the Samples window. Must be constructed on the FX thread (see `SamplesControl.apply`).
class SamplesControl[F[_]: {Async, Dispatcher}](
    sampleGraph: SampleGraph[F],
    samplesTable: SamplesTable[F],
    state: AtomicCell[F, State],
) extends HBox:
  samplesTable.vgrow = Priority.Always

  padding = Insets(10)
  spacing = 10
  children = Seq(
    new VBox:
      spacing = 5
      hgrow = Priority.Always
      children = Seq(Label("Samples"), samplesTable),
  )

  // Table is updated inside `evalUpdate` so concurrent calls can't render the state out of order.
  // Graph shows the first sample of the table, when the first samples are added.
  def samplesAdded(samples: Set[Sample]): F[Unit] = state.evalUpdate: st =>
    for
      _ <- st.samples.keys.assertContainsNoneOf(samples.map(_.id), "Duplicate sample IDs found in added samples")
      newSamples = st.samples ++ samples.map(s => s.id -> s)
      _ <- samplesTable.update(newSamples.values.toList.sortBy(_.id.value))
      _ <- newSamples.values.headOption.traverse_(sampleGraph.render).whenA(st.samples.isEmpty)
    yield st.copy(samples = newSamples)

  def reset: F[Unit] = state.evalUpdate: _ =>
    (samplesTable.clear *> sampleGraph.clear).as(State.empty)

  def showSample(id: SampleId): F[Unit] = state.get.flatMap: st =>
    st.samples.get(id) match
      case Some(sample) => sampleGraph.clear *> sampleGraph.render(sample)
      case None         => s"Sample with ID $id not found".assertionError

object SamplesControl:
  final case class State(samples: Map[SampleId, Sample])

  object State:
    val empty: State = State(Map.empty)

  def apply[F[_]: {Async, Dispatcher}](
      sampleGraph: SampleGraph[F],
      samplesTable: SamplesTable[F],
  ): F[SamplesControl[F]] =
    for
      state <- AtomicCell[F].of(State.empty)
      control <- fx(new SamplesControl(sampleGraph, samplesTable, state))
      _ <- samplesTable.setControl(control)
    yield control
