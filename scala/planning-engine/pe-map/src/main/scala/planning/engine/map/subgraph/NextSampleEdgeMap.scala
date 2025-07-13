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
| created: 2025-06-17 |||||||||||*/

package planning.engine.map.subgraph

import planning.engine.common.values.node.HnId

final case class NextSampleEdgeMap[F[_]](
    currentNodeId: HnId,
    sampleEdges: List[NextSampleEdge[F]]
)
