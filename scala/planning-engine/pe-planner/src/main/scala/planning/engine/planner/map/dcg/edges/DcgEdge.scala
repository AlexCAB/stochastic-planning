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

  def apply[F[_]: MonadThrow](key: Key, edges: List[SampleEdge]): F[DcgEdge[F]] =
    for
      _ <- edges.assertNonEmpty("SampleEdges list is empty")
      edgeKeys = edges.map(e => (e.edgeType, e.source.hnId, e.target.hnId)).toSet
      keyValues = Set((key.edgeType, key.sourceId, key.targetId))
      _ <- (edgeKeys, keyValues).assertSameElems(s"Edge keys from SampleEdges do not match the provided Key: $key")
      _ <- edges.map(_.sampleId).assertDistinct("Duplicate SampleIds in SampleEdges detected")
      _ <- edges.map(_.source.value).assertDistinct("Duplicate Source value in SampleEdges detected")
      _ <- edges.map(_.target.value).assertDistinct("Duplicate Target value in SampleEdges detected")
      samples = edges.map(e => e.sampleId -> Indexies(e.source.value, e.target.value)).toMap
    yield DcgEdge(key, samples)
