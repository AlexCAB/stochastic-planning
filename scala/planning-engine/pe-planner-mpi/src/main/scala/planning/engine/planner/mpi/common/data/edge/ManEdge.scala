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
| created: 16-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.common.data.edge

import planning.engine.common.enums.EdgeType
import planning.engine.planner.mpi.common.data.node.NodeData

// Represent manually defined sample edge between two nodes addressed by their HnName.
final case class ManEdge(source: NodeData, target: NodeData, edgeType: EdgeType)
