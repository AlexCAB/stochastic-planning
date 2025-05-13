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
| created: 2025-05-09 |||||||||||*/

package planning.engine.map.samples

import cats.effect.IO
import neotypes.model.types.{Node, Value}
import planning.engine.common.UnitSpecIO
import planning.engine.map.database.Neo4jQueries.SAMPLES_LABEL
import planning.engine.common.properties.*
import planning.engine.common.values.SampleId

class SamplesStateSpec extends UnitSpecIO:

  "toQueryParams" should:
    "return a map with correct query parameters for valid state" in:
      SamplesState(5L, SampleId(10L), Map.empty).toQueryParams[IO]
        .logValue
        .asserting(_ mustEqual Map(
          PROP_NAME.SAMPLES_COUNT -> 5L.toDbParam,
          PROP_NAME.NEXT_SAMPLES_ID -> 10L.toDbParam
        ))

  "fromProperties" should:
    "create a valid SamplesState from valid properties" in:
      SamplesState
        .fromProperties[IO](
          Map(PROP_NAME.SAMPLES_COUNT -> Value.Integer(5), PROP_NAME.NEXT_SAMPLES_ID -> Value.Integer(10))
        )
        .logValue
        .asserting(_ mustEqual SamplesState(5L, SampleId(10L), Map.empty))

    "raise an error when required properties are missing" in:
      SamplesState.fromProperties[IO](Map(PROP_NAME.SAMPLES_COUNT -> Value.Integer(5)))
        .logValue
        .assertThrows[AssertionError]

    "fromNode should create SamplesState from a valid node" in:
      SamplesState
        .fromNode[IO](Node(
          "1",
          Set(SAMPLES_LABEL),
          Map(PROP_NAME.SAMPLES_COUNT -> Value.Integer(5), PROP_NAME.NEXT_SAMPLES_ID -> Value.Integer(10))
        ))
        .logValue
        .asserting(_ mustEqual SamplesState(5L, SampleId(10L), Map.empty))
