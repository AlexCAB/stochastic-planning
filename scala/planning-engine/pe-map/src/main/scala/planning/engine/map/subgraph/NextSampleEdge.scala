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
| created: 2025-07-06 |||||||||||*/

package planning.engine.map.subgraph

import cats.MonadThrow
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.{SampleData, SampleEdge}
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

final case class NextSampleEdge[F[_]](
    sampleData: SampleData,
    currentValue: HnIndex,
    edgeType: EdgeType,
    nextValue: HnIndex,
    nextHn: HiddenNode[F]
)

object NextSampleEdge:
  def fromSampleEdge[F[_]: MonadThrow](
      edge: SampleEdge,
      sampleDataMap: Map[SampleId, SampleData],
      hnMap: Map[HnId, HiddenNode[F]]
  ): F[NextSampleEdge[F]] =
    for
      sampleData <- sampleDataMap.get(edge.sampleId) match
        case Some(data) => data.pure
        case None       => s"SampleData for sampleId ${edge.sampleId} not found".assertionError
      nextHn <- hnMap.get(edge.target.hnId) match
        case Some(hn) => hn.pure
        case None     => s"HiddenNode for HnId ${edge.target.hnId} not found".assertionError
    yield NextSampleEdge(
      sampleData = sampleData,
      currentValue = edge.source.value,
      edgeType = edge.edgeType,
      nextValue = edge.target.value,
      nextHn = nextHn
    )
