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
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.edge.HiddenEdge.SampleIndexies

final case class DcgEdge(
    key: DcgEdge.Key,
    samples: Map[SampleId, SampleIndexies]
):
  lazy val hnIds: Set[HnId] = Set(key.sourceId, key.targetId)

object DcgEdge:
  final case class Key(
      edgeType: EdgeType,
      sourceId: HnId, // Line
      targetId: HnId  // Arrow
  )

  def apply[F[_]: MonadThrow](
      edge: HiddenEdge
  ): F[DcgEdge] = DcgEdge(
    key = Key(
      edgeType = edge.edgeType,
      sourceId = edge.sourceId,
      targetId = edge.targetId
    ),
    samples = edge.samples.map(s => s.sampleId -> s).toMap
  ).pure
