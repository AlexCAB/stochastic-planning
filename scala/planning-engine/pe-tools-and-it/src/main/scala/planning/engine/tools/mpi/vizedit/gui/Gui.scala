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
import planning.engine.tools.GuiApp.Exit
import planning.engine.tools.mpi.vizedit.adaptors.ControlAdaptor
import planning.engine.tools.mpi.vizedit.config.model.GuiConf
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph
import planning.engine.tools.mpi.vizedit.gui.visualization.Graphs
import planning.engine.tools.mpi.vizedit.gui.windows.{Main, Planning, Samples}

final case class Gui[F[_]](
    mainGraph: NetworkGraph[F],
)

object Gui:
  def apply[F[_]: {Async, Dispatcher}](
      config: GuiConf,
      graphs: Graphs[F],
      control: ControlAdaptor[F],
      exit: Exit[F],
  ): Resource[F, Gui[F]] = Resource.eval:
    for
      samples <- Samples[F](graphs.samples)
      planning <- Planning[F](graphs.planning)
      main <- Main[F](graphs.map, samples, planning, exit)
      _ <- main.window.show
    yield Gui(main.graph)
