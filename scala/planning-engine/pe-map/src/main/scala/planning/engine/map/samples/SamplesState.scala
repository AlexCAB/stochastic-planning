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
| created: 2025-05-01 |||||||||||*/

package planning.engine.map.samples

import cats.MonadThrow
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.common.values.sample.SampleId
import planning.engine.map.database.Neo4jQueries.SAMPLES_LABEL
import planning.engine.map.samples.sample.SampleData

final case class SamplesState(
    sampleCount: Long,
    nextSampleId: SampleId,
    samples: Map[SampleId, SampleData]
):
  private[map] def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP_NAME.SAMPLES_COUNT -> sampleCount.toDbParam, PROP_NAME.NEXT_SAMPLES_ID -> nextSampleId.toDbParam)

object SamplesState:
  private[map] def empty: SamplesState = SamplesState(0L, SampleId.init, Map.empty)

  private[map] def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[SamplesState] =
    for
      sampleCount <- props.getValue[F, Long](PROP_NAME.SAMPLES_COUNT)
      nextSampleId <- props.getValue[F, Long](PROP_NAME.NEXT_SAMPLES_ID).map(SampleId.apply)
    yield SamplesState(sampleCount, nextSampleId, Map.empty)

  private[map] def fromNode[F[_]: MonadThrow](node: Node): F[SamplesState] = node match
    case n if n.is(SAMPLES_LABEL) =>
      for
        sampleCount <- node.getValue[F, Long](PROP_NAME.SAMPLES_COUNT)
        nextSampleId <- node.getValue[F, Long](PROP_NAME.NEXT_SAMPLES_ID).map(SampleId.apply)
      yield SamplesState(sampleCount, nextSampleId, Map.empty)
    case _ => s"Not a samples node, $node".assertionError
