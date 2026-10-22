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
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import planning.engine.tools.GuiApp
import planning.engine.tools.mpi.vizedit.gui.windows.Main

object VizEditTool extends GuiApp:
  protected override def build(closed: Deferred[IO, Unit])(using Dispatcher[IO]): Resource[IO, Unit] =
    Main[IO](closed).void
