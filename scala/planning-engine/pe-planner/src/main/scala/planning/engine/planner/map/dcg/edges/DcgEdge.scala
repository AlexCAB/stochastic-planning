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
| created: 2025-12-01 |||||||||||*/

package planning.engine.planner.map.dcg.edges

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.samples.sample.SampleEdge
import planning.engine.common.errors.*

final case class DcgEdge[F[_]: MonadThrow](
    key: DcgEdge.Key,
    samples: Map[SampleId, DcgEdge.Indexies]
):
  lazy val hnIds: Set[HnId] = Set(key.sourceId, key.targetId)

  def join(other: DcgEdge[F]): F[DcgEdge[F]] =
    if this.key != other.key then s"Cannot join with different keys: ${this.key} and ${other.key}".assertionError
    else
      val intersect = this.samples.keySet.intersect(other.samples.keySet)
      if intersect.nonEmpty then s"Map edge can't have duplicate samples: $intersect".assertionError
      else this.copy(samples = this.samples ++ other.samples).pure

object DcgEdge:
  final case class Key(
      edgeType: EdgeType,
      sourceId: HnId, // Line
      targetId: HnId // Arrow
  )

  final case class Indexies(
      sourceIndex: HnIndex,
      targetIndex: HnIndex
  )

  def apply[F[_]: MonadThrow](edge: HiddenEdge): F[DcgEdge[F]] = DcgEdge(
    key = Key(edgeType = edge.edgeType, sourceId = edge.sourceId, targetId = edge.targetId),
    samples = edge.samples
      .map(s => s.sampleId -> Indexies(sourceIndex = s.sourceIndex, targetIndex = s.targetIndex))
      .toMap
  ).pure

  def apply[F[_]: MonadThrow](edge: SampleEdge): F[DcgEdge[F]] = DcgEdge(
    key = Key(edgeType = edge.edgeType, sourceId = edge.source.hnId, targetId = edge.target.hnId),
    samples = Map(edge.sampleId -> Indexies(sourceIndex = edge.source.value, targetIndex = edge.target.value))
  ).pure
