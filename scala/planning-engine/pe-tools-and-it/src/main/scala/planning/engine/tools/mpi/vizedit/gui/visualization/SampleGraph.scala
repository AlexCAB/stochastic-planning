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
| created: 2026-10-30 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.visualization

import cats.syntax.all.*
import cats.effect.Async
import cats.effect.std.Dispatcher
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.errors.*
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph
import planning.engine.tools.mpi.vizedit.gui.visualization.layouts.NodesView

class SampleGraph[F[_]: {Async, Dispatcher, LoggerFactory}](val graph: NetworkGraph[F]):
  private val logger = LoggerFactory[F].getLogger

  def render(sample: Sample): F[Unit] =
    for
      edgeMnId <- sample.edges.flatMap(_.mnIds).pure
      _ <- sample.nodes.keySet.assertContainsAllOf(edgeMnId, s"Sample ${sample.id} has edges with unknown nodes")
      _ <- graph.render(NodesView(sample.nodes, sample.edges))
      _ <- logger.info(s"SampleGraph rendered sample ${sample.id}")
    yield ()

  def clear: F[Unit] =
    for
      _ <- graph.clear
      _ <- logger.info("SampleGraph cleared")
    yield ()

object SampleGraph:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](): F[SampleGraph[F]] =
    for
      graph <- NetworkGraph[F]()
      _ <- fx(graph.getStyleClass.add("sample-graph"))
    yield new SampleGraph(graph)
