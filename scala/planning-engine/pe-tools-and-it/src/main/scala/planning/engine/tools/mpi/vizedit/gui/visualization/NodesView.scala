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
| created: 2026-11-01 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.visualization

import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph
import planning.engine.tools.mpi.vizedit.gui.visualization.layouts.SampleLayout

// Styled and layered view of map nodes and edges, common for the sample and the map graphs.
object NodesView:
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

  // All nodes of the `edges` must be in the `nodes`.
  def apply(nodes: Map[MnId, NodeData], edges: Set[MeKey]): NetworkGraph.View =
    val labels = nodes.map((id, data) => id -> nodeLabel(id, data.name, data.description))
    val positions = SampleLayout(nodes.keySet, edges.map(k => (k.src, k.trg, k.asEdgeType)))

    NetworkGraph.View(
      vertices = nodes.toList.map((id, _) => NetworkGraph.Vertex(labels(id), nodeStyle(id), positions(id))),
      edges = edges.toList.map: key =>
        val edgeType = key.asEdgeType
        val element = s"${key.src}-$edgeType->${key.trg}"
        NetworkGraph.Edge(element, labels(key.src), labels(key.trg), edgeStyle(edgeType), arrowStyle(edgeType)),
    )
