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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.samples.sample.observed

import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.{Description, Name}

final case class ObservedSample(
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description],
    hnIds: List[HnId],
    edges: List[ObservedEdge]
)
