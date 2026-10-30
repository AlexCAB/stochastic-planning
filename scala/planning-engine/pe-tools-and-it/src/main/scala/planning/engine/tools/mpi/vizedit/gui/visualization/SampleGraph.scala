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
import planning.engine.common.enums.EdgeType
import planning.engine.common.errors.*
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.planner.mpi.model.data.visualization.SampleViz
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph

final case class SampleGraph[F[_]: {Async, Dispatcher, LoggerFactory}](graph: NetworkGraph[F]):
  private val logger = LoggerFactory[F].getLogger

  // Node ID is part of the label, since node name is optional and not guaranteed to be unique.
  private def nodeLabel(id: MnId, name: Option[HnName], description: Option[Description]): String =
    id.toString + name.map(n => s" ${n.value}").mkString + description.map(d => s" ${d.value}").mkString

  private def nodeStyle(id: MnId): String = if id.isCon then "vertex-concrete" else "vertex-abstract"

  private def edgeStyle(edgeType: EdgeType): String = edgeType match
    case EdgeType.LINK => "edge-link"
    case EdgeType.THEN => "edge-then"

  private def toView(sample: SampleViz): NetworkGraph.View =
    val labels = sample.nodes.map((id, name, description) => id -> nodeLabel(id, name, description)).toMap

    NetworkGraph.View(
      vertices = sample.nodes.toList.map((id, _, _) => NetworkGraph.Vertex(labels(id), nodeStyle(id))),
      edges = sample.edges.toList.map: (src, trg, edgeType) =>
        NetworkGraph.Edge(s"$src-$edgeType->$trg", labels(src), labels(trg), edgeStyle(edgeType)),
    )

  def render(sample: SampleViz): F[Unit] =
    for
      edgeMnId <- sample.edges.flatMap((src, trg, _) => Set(src, trg)).pure
      _ <- sample.mnId.assertContainsAllOf(edgeMnId, s"Sample ${sample.id} has edges with unknown nodes")
      _ <- graph.render(toView(sample))
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
    yield new SampleGraph(graph)
