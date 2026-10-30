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
      samples <- samples.getConfig
      planning <- planning.getConfig
    yield GuiConf(
      main = main,
      samples = samples,
      planning = planning,
    )

  // Windows' `onHidden` callbacks run through the `Dispatcher`, so all windows must be closed while it is still open,
  // otherwise `Platform.exit()` hides the remaining ones after the dispatcher is closed.
  def close: F[Unit] = List(samples.window.close, planning.window.close, main.window.close).sequence_

object Gui:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](
      config: GuiConf,
      control: ControlAdaptor[F],
      visualization: VisualizerAdaptor[F],
      exit: Exit[F],
  ): Resource[F, Gui[F]] =
    val acquire =
      for
        samples <- Samples[F](config.samples)
        planning <- Planning[F](config.planning)
        main <- Main[F](config.main, control, visualization, samples, planning, exit)
        gui = Gui(samples, planning, main)
        _ <- visualization.setGui(gui)
        _ <- samples.window.show.whenA(config.samples.isShown)
        _ <- planning.window.show.whenA(config.planning.isShown)
        _ <- main.window.show // Shown last, to be on top of the other windows.
      yield gui

    Resource.make(acquire)(_.close)
