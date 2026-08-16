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

package planning.engine.common.graph.edges

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.HnName

// Represent manually defined sample edge between two nodes addressed by their HnName.
final case class ManEdge(source: HnName, target: HnName, edgeType: EdgeType)
