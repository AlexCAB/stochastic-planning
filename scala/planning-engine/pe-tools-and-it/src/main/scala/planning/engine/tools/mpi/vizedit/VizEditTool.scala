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

object VizEditTool extends GuiApp:
  protected override def build(argStr: List[String], exit: Exit[IO])(using Dispatcher[IO]): Resource[IO, Unit] =
    for
      args <- Arguments.parse(argStr)
      guiConf <- ConfigReader.readOrDefault()
      visualization <- VisualizerAdaptor()
      map <- MapMpi(Some(visualization))
      control <- ControlAdaptor(args, map)
      gui <- Gui(guiConf, control, visualization, exit)
      _ <- ConfigWriter[IO](gui)
      _ <- Resource.eval(control.init)
    yield ()
