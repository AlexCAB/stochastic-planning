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
| created: 2026-10-31 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.visualization

import cats.syntax.all.*
import cats.effect.Async
import cats.effect.std.Dispatcher
import org.typelevel.log4cats.LoggerFactory
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph

class MapGraph[F[_]: {Async, Dispatcher, LoggerFactory}](val graph: NetworkGraph[F]):
  private val logger = LoggerFactory[F].getLogger

  def clear: F[Unit] =
    for
      _ <- graph.clear
      _ <- logger.info("SampleGraph cleared")
    yield ()

object MapGraph:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](): F[MapGraph[F]] =
    for
        graph <- NetworkGraph[F]()
    yield new MapGraph(graph)
