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
| created: 2026-10-07 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.HnId

final case class MpiNewEdges(
    edges: Set[(HnId, HnId, EdgeType)],
) extends VisualizationMsg
