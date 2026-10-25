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
import planning.engine.tools.mpi.vizedit.gui.controls.PlanningControl

final case class Planning[F[_]: {Async, Dispatcher}](
    graph: NetworkGraph[F],
    control: PlanningControl,
    window: Window[F],
)

object Planning:
  def apply[F[_]: {Async, Dispatcher}](config: Window.Config): F[Planning[F]] =
    for
      graph <- NetworkGraph[F]()
      control <- PlanningControl()
      window <- Window[F]("Planning", config, graph, control, Async[F].unit)
    yield new Planning(graph, control, window)
