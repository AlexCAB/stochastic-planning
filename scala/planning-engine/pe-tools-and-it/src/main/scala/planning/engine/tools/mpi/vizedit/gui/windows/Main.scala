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
import planning.engine.tools.GuiApp.Exit
import planning.engine.tools.mpi.vizedit.gui.components.{NetworkGraph, Window}
import planning.engine.tools.mpi.vizedit.gui.controls.MainControl

final case class Main[F[_]: {Async, Dispatcher}](
    graph: NetworkGraph[F],
    control: MainControl[F],
    window: Window[F],
)

object Main:
  def apply[F[_]: {Async, Dispatcher}](
      graph: NetworkGraph[F],
      samples: Samples[F],
      planning: Planning[F],
      exit: Exit[F],
  ): F[Main[F]] =
    for
      control <- MainControl[F](graph, samples, planning)
      window <- Window[F]("Map Network Visual Edit Tool", graph, control, exit.complete(()).void)
    yield new Main(graph, control, window)
