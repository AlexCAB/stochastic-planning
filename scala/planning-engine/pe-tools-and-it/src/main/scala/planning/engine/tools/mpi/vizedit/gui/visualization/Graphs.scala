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
| created: 2026-10-24 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.visualization

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph

final case class Graphs[F[_]: {Async, Dispatcher}](
    map: NetworkGraph[F],
    samples: NetworkGraph[F],
    planning: NetworkGraph[F],
)

object Graphs:
  def apply[F[_]: {Async, Dispatcher}](): Resource[F, Graphs[F]] = Resource.eval:
    for
      map <- NetworkGraph()
      samples <- NetworkGraph()
      planning <- NetworkGraph()
    yield new Graphs(map, samples, planning)
