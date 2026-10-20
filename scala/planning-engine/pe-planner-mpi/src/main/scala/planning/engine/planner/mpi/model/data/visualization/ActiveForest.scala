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

import planning.engine.common.graph.inference.PU
import planning.engine.common.values.node.MnId

// Contain found the active forest for visualization.
final case class ActiveForest(
    nodes: Map[MnId, PU],
    roots: Set[MnId],
    edges: Set[(MnId, MnId)],
)
