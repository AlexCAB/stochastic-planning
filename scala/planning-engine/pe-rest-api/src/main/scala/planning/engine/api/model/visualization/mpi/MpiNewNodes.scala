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
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnId

final case class MpiNewNodes(
    conNodes: Set[HnId],
    absNodes: Set[HnId],
    ioEdges: Set[(IoName, Set[HnId])],
) extends VisualizationMsg
