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

package planning.engine.map.samples.sample.stored

import cats.MonadThrow
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.observed.ObservedSample
import planning.engine.common.errors.assertSameElems
import cats.syntax.all.*

final case class StoredSample(
    data: SampleData,
    edges: List[SampleEdge]
):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] = paramsOf(
    PROP.SAMPLE_ID -> data.id.toDbParam,
    PROP.PROBABILITY_COUNT -> data.probabilityCount.toDbParam,
    PROP.UTILITY -> data.utility.toDbParam,
    PROP.NAME -> data.name.map(_.toDbParam),
    PROP.DESCRIPTION -> data.description.map(_.toDbParam)
  )

object StoredSample:
  def fromObservedSample[F[_]: MonadThrow](
      sample: ObservedSample,
      sampleId: SampleId,
      hnIndexies: Map[HnId, HnIndex]
  ): F[StoredSample] =
    for
        _ <- (hnIndexies.keySet, sample.hnIds).assertSameElems("Collection hnIndexies have not all HN IDs from sample")
    yield StoredSample(
      data = SampleData(
        id = sampleId,
        probabilityCount = sample.probabilityCount,
        utility = sample.utility,
        name = sample.name,
        description = sample.description
      ),
      edges = sample.edges.map(edge =>
        SampleEdge(
          sourceHn = edge.source,
          targetHn = edge.target,
          sourceValue = hnIndexies(edge.source),
          targetValue = hnIndexies(edge.target),
          edgeType = edge.edgeType,
          sampleId = sampleId
        )
      )
    )
