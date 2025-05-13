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

package planning.engine.map.knowledge.graph

import cats.effect.IO
import neotypes.model.types.{Node, Value}
import planning.engine.common.UnitSpecIO
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL
import planning.engine.common.properties.*
import planning.engine.common.values.node.HnId

class KnowledgeGraphStateSpec extends UnitSpecIO:

  "toQueryParams" should:
    "return a map with correct query parameters for valid state" in:
      KnowledgeGraphState(HnId(10L)).toQueryParams[IO]
        .logValue
        .asserting(_ mustEqual Map(PROP_NAME.NEXT_HN_ID -> 10L.toDbParam))

  "fromProperties" should:
    "create a valid KnowledgeGraphState from valid properties" in:
      KnowledgeGraphState
        .fromProperties[IO](Map(PROP_NAME.NEXT_HN_ID -> Value.Integer(10)))
        .logValue
        .asserting(_ mustEqual KnowledgeGraphState(HnId(10L)))

    "raise an error when required properties are missing" in:
      KnowledgeGraphState.fromProperties[IO](Map.empty)
        .logValue
        .assertThrows[AssertionError]

  "fromNode" should:
    "create KnowledgeGraphState from a valid root node" in:
      KnowledgeGraphState
        .fromNode[IO](Node("1", Set(ROOT_LABEL), Map(PROP_NAME.NEXT_HN_ID -> Value.Integer(10))))
        .logValue
        .asserting(_ mustEqual KnowledgeGraphState(HnId(10L)))

    "raise an error for a node without the root label" in:
      KnowledgeGraphState
        .fromNode[IO](Node("1", Set("OtherLabel"), Map(PROP_NAME.NEXT_HN_ID -> Value.Integer(10))))
        .logValue
        .assertThrows[AssertionError]
