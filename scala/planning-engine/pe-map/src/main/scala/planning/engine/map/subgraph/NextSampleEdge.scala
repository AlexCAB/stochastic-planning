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
| created: 2025-07-06 |||||||||||*/

package planning.engine.map.subgraph

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.HnIndex
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.SampleData

final case class NextSampleEdge[F[_]](
    sampleData: SampleData,
    currentValue: HnIndex,
    edgeType: EdgeType,
    nextValue: HnIndex,
    nextHn: HiddenNode[F]
)
