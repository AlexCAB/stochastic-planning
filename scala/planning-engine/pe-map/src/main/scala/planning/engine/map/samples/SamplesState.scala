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
import planning.engine.common.values.SampleId
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.map.database.Neo4jQueries.SAMPLES_LABEL
import planning.engine.map.samples.sample.SampleData

final case class SamplesState(
    sampleCount: Long,
    nextSampleId: SampleId,
    samples: Map[SampleId, SampleData]
):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP_NAME.SAMPLES_COUNT -> sampleCount.toDbParam, PROP_NAME.NEXT_SAMPLES_ID -> nextSampleId.toDbParam)

object SamplesState:
  def empty: SamplesState = SamplesState(0L, SampleId.init, Map.empty)

  def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[SamplesState] =
    for
      sampleCount <- props.getValue[F, Long](PROP_NAME.SAMPLES_COUNT)
      nextSampleId <- props.getValue[F, Long](PROP_NAME.NEXT_SAMPLES_ID).map(SampleId.apply)
    yield SamplesState(sampleCount, nextSampleId, Map.empty)

  def fromNode[F[_]: MonadThrow](node: Node): F[SamplesState] = node match
    case Node(_, labels, props) if labels.exists(_.equalsIgnoreCase(SAMPLES_LABEL)) => fromProperties[F](props)
    case _ => s"Not a samples node, $node".assertionError
