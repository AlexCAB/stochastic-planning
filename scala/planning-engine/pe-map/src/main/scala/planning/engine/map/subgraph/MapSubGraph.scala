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
| created: 2025-12-06 |||||||||||*/

package planning.engine.map.subgraph

import cats.MonadThrow
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.SampleData

final case class MapSubGraph[F[_]: MonadThrow](
    skippedNodes: List[HnId],
    loadedNodes: List[HiddenNode[F]],
    edges: List[HiddenEdge],
    samplesData: List[SampleData]
)
