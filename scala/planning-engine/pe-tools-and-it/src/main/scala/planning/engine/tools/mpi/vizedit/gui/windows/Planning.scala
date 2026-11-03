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
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.gui.components.Window
import planning.engine.tools.mpi.vizedit.gui.controls.PlanningControl
import planning.engine.tools.mpi.vizedit.gui.visualization.PlanningGraph

class Planning[F[_]: {Async, Dispatcher}](
    val planningGraph: PlanningGraph[F],
    val control: PlanningControl[F],
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

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](config: Config): F[Planning[F]] =
    for
      planningGraph <- PlanningGraph[F]()
      control <- PlanningControl[F]()
      window <- Window[F]("Planning", config.window, planningGraph.graph, control, Async[F].unit)
    yield new Planning(planningGraph, control, window)
