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
import planning.engine.tools.GuiApp.Exit
import planning.engine.tools.mpi.vizedit.adaptors.{ControlAdaptor, VisualizerAdaptor}
import planning.engine.tools.mpi.vizedit.gui.components.{IoVarsTable, NetworkGraph, Window}
import planning.engine.tools.mpi.vizedit.gui.controls.MainControl

import java.nio.file.Path

class Main[F[_]: {Async, Dispatcher, LoggerFactory}](
    val graph: NetworkGraph[F],
    val ioVarsTable: IoVarsTable[F],
    val control: MainControl[F],
    val window: Window[F],
):
  def getConfig: F[Main.Config] =
    for
      window <- window.getConfig
      workDir <- control.getWorkDir
    yield Main.Config(window = window, workDir = workDir)

object Main:
  final case class Config(
      window: Window.Config,
      workDir: Path,
  )

  val title: String = "Map Network Visual Edit Tool"

  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](
      config: Config,
      control: ControlAdaptor[F],
      visualization: VisualizerAdaptor[F],
      samples: Samples[F],
      planning: Planning[F],
      exit: Exit[F],
  ): F[Main[F]] =
    for
      graph <- NetworkGraph[F]()
      ioVarsTable <- IoVarsTable[F]()
      control <- MainControl[F](config.workDir, graph, ioVarsTable, control, visualization, samples, planning)
      window <- Window[F](title, config.window, graph, control, exit.complete(()).void)
    yield new Main(graph, ioVarsTable, control, window)
