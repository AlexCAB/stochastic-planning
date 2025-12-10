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

final case class CachedEdge(
    edgeType: EdgeType,
    sourceId: HnId, // Line
    targetId: HnId, // Arrow
    samples: Map[SampleId, SampleIndexies]
)

object CachedEdge:
  def apply[F[_]: MonadThrow](
      edge: HiddenEdge
  ): F[CachedEdge] =
    CachedEdge(
      edgeType = edge.edgeType,
      sourceId = edge.sourceId,
      targetId = edge.targetId,
      samples = edge.samples.map(s => s.sampleId -> s).toMap
    ).pure
