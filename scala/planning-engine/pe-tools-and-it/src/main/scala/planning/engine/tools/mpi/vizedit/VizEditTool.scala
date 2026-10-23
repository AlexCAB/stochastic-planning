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
import planning.engine.tools.GuiApp
import planning.engine.tools.GuiApp.Exit
import planning.engine.tools.mpi.vizedit.config.{ConfigReader, ConfigWriter}
import planning.engine.tools.mpi.vizedit.gui.Gui

object VizEditTool extends GuiApp:
  protected override def build(exit: Exit[IO])(using Dispatcher[IO]): Resource[IO, Unit] =
    for
      guiConf <- ConfigReader.readOrDefault()
      gui <- Gui(guiConf, exit)
      _ <- ConfigWriter[IO](gui)
    yield ()
