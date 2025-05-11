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
| created: 2025-04-10 |||||||||||*/

package planning.engine.map.knowledge.graph

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import neotypes.model.types.{Node, Value}
import planning.engine.common.values.{OpDescription, OpName}
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL

import planning.engine.common.properties.QueryParamMapping.*

class MetadataSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val metadataProps = Map(
      "name" -> Value.Str("TestName"),
      "description" -> Value.Str("TestDescription")
    )

    val metadataParams = metadataProps.map:
      case (k, v) => k -> v.toParam

    val metadata = Metadata(OpName(Some("TestName")), OpDescription(Some("TestDescription")))

    val node = Node(
      "1",
      Set(ROOT_LABEL),
      Map(
        "name" -> Value.Str("TestName"),
        "description" -> Value.Str("TestDescription")
      )
    )

  "toQueryParam" should:
    "return correct properties map for metadata" in newCase[CaseData]: data =>
      data.metadata.toQueryParams[IO]
        .logValue
        .asserting(_ mustEqual data.metadataParams)

  "fromProperties" should:
    "create metadata from properties" in newCase[CaseData]: data =>
      Metadata.fromProperties[IO](data.metadataProps)
        .logValue
        .asserting(_ mustEqual data.metadata)

    "fromNode should create Metadata from a valid root node" in newCase[CaseData]: data =>
      Metadata
        .fromNode[IO](data.node)
        .logValue
        .asserting(_ mustEqual Metadata("TestName", "TestDescription"))
