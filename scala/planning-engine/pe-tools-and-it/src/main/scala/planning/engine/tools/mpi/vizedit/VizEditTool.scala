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
| created: 2026-10-21 |||||||||||*/

package planning.engine.tools.mpi.vizedit

import cats.effect.*
import cats.effect.std.Dispatcher
import planning.engine.planner.mpi.MapMpi
import planning.engine.tools.GuiApp
import planning.engine.tools.GuiApp.Exit
import planning.engine.tools.mpi.vizedit.adaptors.{ControlAdaptor, VisualizerAdaptor}
import planning.engine.tools.mpi.vizedit.arguments.Arguments
import planning.engine.tools.mpi.vizedit.config.{ConfigReader, ConfigWriter}
import planning.engine.tools.mpi.vizedit.gui.Gui
import planning.engine.tools.mpi.vizedit.gui.visualization.Graphs

object VizEditTool extends GuiApp:
  protected override def build(argStr: List[String], exit: Exit[IO])(using Dispatcher[IO]): Resource[IO, Unit] =
    for
      args <- Arguments.parse(argStr)
      guiConf <- ConfigReader.readOrDefault()
      graphs <- Graphs[IO]()
      visualization <- VisualizerAdaptor(graphs)
      map <- MapMpi(Some(visualization))
      control <- ControlAdaptor(map)
      gui <- Gui(guiConf, graphs, control, exit)
      _ <- ConfigWriter[IO](gui)
    yield ()
