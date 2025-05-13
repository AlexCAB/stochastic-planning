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
| created: 2025-04-07 |||||||||||*/

package planning.engine.map.samples.sample

import planning.engine.common.values.Index
import planning.engine.map.samples.sample.SampleData

case class SampleEdgeState(
                            sourceValue: Index,
                            targetValue: Index,
                            sample: SampleData
):
  override def toString: String = s"SampleEdgeState($sourceValue -- ${sample.id} --> $targetValue)"
