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

package planning.engine.tools.mpi.vizedit.gui.windows

import cats.effect.Async
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import planning.engine.tools.mpi.vizedit.gui.components.{NetworkGraph, Window}
import planning.engine.tools.mpi.vizedit.gui.controls.SamplesControl

final case class Samples[F[_]: {Async, Dispatcher}](
    graph: NetworkGraph[F],
    control: SamplesControl,
    window: Window[F],
)

object Samples:
  def apply[F[_]: {Async, Dispatcher}](graph: NetworkGraph[F]): F[Samples[F]] =
    for
      control <- SamplesControl()
      window <- Window[F]("Samples", graph, control)
    yield new Samples(graph, control, window)
