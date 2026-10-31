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

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.MnId
import planning.engine.tools.mpi.vizedit.gui.components.NetworkGraph.Position

// Layered layout of a sample, same as in the yEd maps (see vizedit/yed/two-way-walk.graphml):
//  - layer 0 (bottom): concrete nodes, ordered in columns by THEN edges;
//  - next layers: abstract nodes, each one layer above the abstract nodes linked to it;
//  - abstract node is centered over the nodes linked to it, or placed as concrete if there are none.
object SampleLayout:
  // Longest path to each of the nodes. Number of passes is bounded, so cycles in `arcs` can't hang it.
  private def longestPaths(ids: Set[MnId], arcs: Set[(MnId, MnId)]): Map[MnId, Int] =
    def relax(paths: Map[MnId, Int]): Map[MnId, Int] = arcs.foldLeft(paths): (acc, arc) =>
      acc.updated(arc._2, acc(arc._2).max(acc(arc._1) + 1))

    Iterator.iterate(ids.map(_ -> 0).toMap)(relax).drop(ids.size).next()

  private def arcsOf(edges: Set[(MnId, MnId, EdgeType)], edgeType: EdgeType): Set[(MnId, MnId)] = edges
    .collect { case (src, trg, tp) if tp == edgeType => src -> trg }

  private def findLayers(ids: Set[MnId], links: Set[(MnId, MnId)]): Map[MnId, Int] =
    val (conIds, absIds) = ids.partition(_.isCon)
    val absLevels = longestPaths(absIds, links.filter((src, trg) => absIds(src) && absIds(trg)))
    conIds.map(_ -> 0).toMap ++ absLevels.map((id, level) => id -> (level + 1))

  // Nodes of the layer, ordered by preferred column, are shifted right as needed to not overlap.
  private def spread(preferred: List[(MnId, Double)]): List[(MnId, Double)] = preferred
    .sortBy((id, column) => (column, id.value))
    .foldLeft(List.empty[(MnId, Double)]):
      case (Nil, node)                         => List(node)
      case (acc @ (_, last) :: _, (id, column)) => (id -> column.max(last + 1.0)) :: acc

  def apply(ids: Set[MnId], edges: Set[(MnId, MnId, EdgeType)]): Map[MnId, Position] =
    val links = arcsOf(edges, EdgeType.LINK)
    val layers = findLayers(ids, links)
    val order = longestPaths(ids, arcsOf(edges, EdgeType.THEN))

    def preferredColumn(placed: Map[MnId, Double])(id: MnId): (MnId, Double) =
      val linked = links.collect { case (src, `id`) if placed.contains(src) => placed(src) }
      id -> (if id.isCon || linked.isEmpty then order(id).toDouble else (linked.min + linked.max) / 2.0)

    val columns = layers.groupMap(_._2)(_._1).toList.sortBy(_._1).foldLeft(Map.empty[MnId, Double]):
      (placed, layer) => placed ++ spread(layer._2.toList.map(preferredColumn(placed)))

    ids.map(id => id -> Position(columns(id), layers(id))).toMap
