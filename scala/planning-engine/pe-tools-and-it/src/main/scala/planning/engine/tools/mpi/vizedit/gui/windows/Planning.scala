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
import planning.engine.tools.mpi.vizedit.adaptors.ControlAdaptor
import planning.engine.tools.mpi.vizedit.gui.components.Window
import planning.engine.tools.mpi.vizedit.gui.controls.PlanningControl
import planning.engine.tools.mpi.vizedit.gui.visualization.graphs.PlanningGraphs

class Planning[F[_]: {Async, Dispatcher}](
    val planningGraph: PlanningGraphs[F],
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

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](
      config: Config,
      controlAdaptor: ControlAdaptor[F],
  ): F[Planning[F]] =
    for
      planningGraphs <- PlanningGraphs[F]()
      control <- PlanningControl[F](controlAdaptor, planningGraphs)
      window <- Window[F]("Planning", config.window, planningGraphs, control, Async[F].unit)
    yield new Planning(planningGraphs, control, window)
