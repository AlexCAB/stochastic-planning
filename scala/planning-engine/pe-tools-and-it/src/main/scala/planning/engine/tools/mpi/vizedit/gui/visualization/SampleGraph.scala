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
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
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

  private def arrowStyle(edgeType: EdgeType): Option[String] = edgeType match
    case EdgeType.LINK => None
    case EdgeType.THEN => Some("arrow-then")

  private def toView(sample: Sample): NetworkGraph.View =
    val labels = sample.nodes.map((id, data) => id -> nodeLabel(id, data.name, data.description))
    val positions = SampleLayout(sample.nodes.keySet, sample.edges.map(k => (k.src, k.trg, k.asEdgeType)))

    NetworkGraph.View(
      vertices = sample.nodes.toList.map((id, _) => NetworkGraph.Vertex(labels(id), nodeStyle(id), positions(id))),
      edges = sample.edges.toList.map: key =>
        val edgeType = key.asEdgeType
        val element = s"${key.src}-$edgeType->${key.trg}"
        NetworkGraph.Edge(element, labels(key.src), labels(key.trg), edgeStyle(edgeType), arrowStyle(edgeType)),
    )

  def render(sample: Sample): F[Unit] =
    for
      edgeMnId <- sample.edges.flatMap(_.mnIds).pure
      _ <- sample.nodes.keySet.assertContainsAllOf(edgeMnId, s"Sample ${sample.id} has edges with unknown nodes")
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
      _ <- fx(graph.getStyleClass.add("sample-graph"))
    yield new SampleGraph(graph)
