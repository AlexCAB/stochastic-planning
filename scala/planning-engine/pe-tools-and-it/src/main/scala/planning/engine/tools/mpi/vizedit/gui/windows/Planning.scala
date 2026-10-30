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

class Planning[F[_]: {Async, Dispatcher}](
    val graph: NetworkGraph[F],
    val control: PlanningControl,
    val window: Window[F],
):
  def getConfig: F[Planning.Config] =
    for
      isShown <- window.isShown
      window <- window.getConfig
    yield Planning.Config(isShown = isShown, window = window)

object Planning:
  final case class Config(
      isShown: Boolean,
      window: Window.Config,
  )

  def apply[F[_]: {Async, Dispatcher}](config: Config): F[Planning[F]] =
    for
      graph <- NetworkGraph[F]()
      control <- PlanningControl()
      window <- Window[F]("Planning", config.window, graph, control, Async[F].unit)
    yield new Planning(graph, control, window)
