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

package planning.engine.tools.mpi.vizedit.gui

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.GuiApp.Exit
import planning.engine.tools.mpi.vizedit.adaptors.ControlAdaptor
import planning.engine.tools.mpi.vizedit.config.model.GuiConf
import planning.engine.tools.mpi.vizedit.gui.visualization.Graphs
import planning.engine.tools.mpi.vizedit.gui.windows.{Main, Planning, Samples}

final case class Gui[F[_]: {Async, LoggerFactory}](
    samples: Samples[F],
    planning: Planning[F],
    main: Main[F],
):
  def getConfig: F[GuiConf] =
    for
      main <- main.window.getConfig
      samples <- samples.window.getConfig
      planning <- planning.window.getConfig
    yield GuiConf(
      main = main,
      samples = samples,
      planning = planning,
    )

object Gui:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](
      config: GuiConf,
      graphs: Graphs[F],
      control: ControlAdaptor[F],
      exit: Exit[F],
  ): Resource[F, Gui[F]] = Resource.eval:
    for
      samples <- Samples[F](config.samples, graphs.samples)
      planning <- Planning[F](config.planning, graphs.planning)
      main <- Main[F](config.main, graphs.map, control, samples, planning, exit)
      _ <- main.window.show
    yield Gui(samples, planning, main)
