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
import planning.engine.tools.mpi.vizedit.adaptors.{ControlAdaptor, VisualizerAdaptor}
import planning.engine.tools.mpi.vizedit.config.model.GuiConf
import planning.engine.tools.mpi.vizedit.gui.windows.{Main, Planning, Samples}

final case class Gui[F[_]: {Async, LoggerFactory}](
    samples: Samples[F],
    planning: Planning[F],
    main: Main[F],
):
  def getConfig: F[GuiConf] =
    for
      main <- main.getConfig
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
      control: ControlAdaptor[F],
      visualization: VisualizerAdaptor[F],
      exit: Exit[F],
  ): Resource[F, Gui[F]] = Resource.eval:
    for
      samples <- Samples[F](config.samples)
      planning <- Planning[F](config.planning)
      main <- Main[F](config.main, control, visualization, samples, planning, exit)
      gui = Gui(samples, planning, main)
      _ <- visualization.setGui(gui)
      _ <- main.window.show
    yield gui
