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
| created: 2025-12-07 |||||||||||*/

package planning.engine.map.hidden.edge

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies

final case class HiddenEdge(
    edgeType: EdgeType,
    sourceId: HnId,
    targetId: HnId,
    samples: List[SampleIndexies]
)

object HiddenEdge:
  final case class SampleIndexies(
      sampleId: SampleId,
      sourceIndex: HnIndex,
      targetIndex: HnIndex
  )
