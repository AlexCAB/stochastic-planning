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
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.model.data.visualization.SampleViz
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.SamplesTable
import scalafx.geometry.Insets
import scalafx.scene.control.Label
import scalafx.scene.layout.{HBox, Priority, VBox}
import SamplesControl.State

// Control area of the Samples window. Must be constructed on the FX thread (see `SamplesControl.apply`).
class SamplesControl[F[_]: {Async, Dispatcher}](
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
  def samplesAdded(samples: Set[SampleViz]): F[Unit] = state.evalUpdate: st =>
    val newSt = st.copy(samples = st.samples ++ samples.map(s => s.id -> s))
    samplesTable.update(newSt.samples.values).as(newSt)

  def reset: F[Unit] = state.evalUpdate: _ =>
    samplesTable.clear.as(State.empty)

object SamplesControl:
  final case class State(samples: Map[SampleId, SampleViz])

  object State:
    val empty: State = State(Map.empty)

  def apply[F[_]: {Async, Dispatcher}](samplesTable: SamplesTable[F]): F[SamplesControl[F]] =
    for
      state <- AtomicCell[F].of(State.empty)
      control <- fx(new SamplesControl(samplesTable, state))
    yield control
