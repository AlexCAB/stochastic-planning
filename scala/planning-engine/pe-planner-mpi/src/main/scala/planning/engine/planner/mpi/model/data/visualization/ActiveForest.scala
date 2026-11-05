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
| created: 2026-10-20 |||||||||||*/

package planning.engine.planner.mpi.model.data.visualization

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.model.data.node.NodeData

// Contain found the active forest for visualization.
final case class ActiveForest(
    nodes: Map[MnId, ActiveForest.Node],
    roots: Set[MnId],
    edges: Set[MeKey],
)

object ActiveForest:
  final case class Node(data: NodeData, pu: PU)
