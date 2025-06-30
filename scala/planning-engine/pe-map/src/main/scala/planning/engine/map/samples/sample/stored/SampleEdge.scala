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

package planning.engine.map.samples.sample.stored

import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId

final case class SampleEdge(
    sourceHn: HnId,
    targetHn: HnId,
    sourceValue: HnIndex,
    targetValue: HnIndex,
    edgeType: EdgeType,
    sampleId: SampleId
):
  lazy val toParams: (String, List[Long]) = (sampleId.value.toString, List(sourceValue.value, targetValue.value))

  override def toString: String =
    s"SampleEdge($sourceHn:$sourceValue -- $sampleId($edgeType) -> $targetHn:$targetValue)"
