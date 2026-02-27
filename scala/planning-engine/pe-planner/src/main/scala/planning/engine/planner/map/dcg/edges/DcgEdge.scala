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
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.common.errors.*
import planning.engine.common.graph.edges.{EdgeKey, IndexMap}
import planning.engine.planner.map.dcg.repr.DcgEdgeRepr

final case class DcgEdge[F[_]: MonadThrow](
    key: EdgeKey,
    samples: DcgSamples[F]
) extends DcgEdgeRepr[F]:
  lazy val mnIds: Set[MnId] = Set(key.src, key.trg)

  lazy val edgeType: EdgeType = key match
    case _: EdgeKey.Link => EdgeType.LINK
    case _: EdgeKey.Then => EdgeType.THEN

  def join(other: DcgEdge[F]): F[DcgEdge[F]] =
    for
      _ <- key.assertEqual(other.key, "Can't join DcgEdges with different keys")
      newSamples <- samples.join(other.samples)
    yield this.copy(samples = newSamples)

  def isActive(sampleIds: Set[SampleId]): Boolean = samples.sampleIds.exists(sampleIds.contains)

  override lazy val toString: String =
    s"$edgeType(${key.src.reprNode}->${key.trg.reprNode}, {$samples})"

object DcgEdge:
  def apply[F[_]: MonadThrow](edge: HiddenEdge, conIds: Set[MnId.Con], absIds: Set[MnId.Abs]): F[DcgEdge[F]] =
    for
      srcMnId <- edge.sourceId.toMnId(conIds, absIds)
      trgMnId <- edge.targetId.toMnId(conIds, absIds)
      samples <- DcgSamples.fromSamples(edge.samples)
    yield edge.edgeType match
      case EdgeType.LINK => new DcgEdge(EdgeKey.Link(srcMnId, trgMnId), samples)
      case EdgeType.THEN => new DcgEdge(EdgeKey.Then(srcMnId, trgMnId), samples)

  def apply[F[_]: MonadThrow](key: EdgeKey, ids: Map[SampleId, IndexMap]): F[DcgEdge[F]] =
    DcgSamples.fromIndexMap(key, ids).map(samples => new DcgEdge(key, samples))
