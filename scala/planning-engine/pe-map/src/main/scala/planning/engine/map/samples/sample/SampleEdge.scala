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

import cats.MonadThrow
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

final case class SampleEdge(
    sourceHn: HnId,
    targetHn: HnId,
    sourceValue: HnIndex,
    targetValue: HnIndex,
    edgeType: EdgeType,
    sampleId: SampleId
):
  override def toString: String =
    s"SampleEdge($sourceHn:$sourceValue -- $sampleId($edgeType) -> $targetHn:$targetValue)"

object SampleEdge:
  final case class New(
      source: HnId,
      target: HnId,
      edgeType: EdgeType
  ):

    def toQueryParams[F[_]: MonadThrow](sampleId: SampleId, indexies: Map[HnId, HnIndex]): F[(String, List[Long])] =
      def getIndex(hnId: HnId): F[HnIndex] = indexies.get(hnId) match
        case Some(id) => id.pure
        case _ => s"Missing HnIndex for $hnId in $indexies".assertionError

      for
        sourceValue <- getIndex(source)
        targetValue <- getIndex(target)
      yield (sampleId.value.toString, List(sourceValue.value, targetValue.value))

    override def toString: String = s"ObservedEdge($source --$edgeType-> target=$target)"
