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

package planning.engine.map.graph

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.PROP_NAME
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL
import planning.engine.common.properties.PropertiesMapping.*
import planning.engine.common.values.text.{Description, Name}

class MetadataSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val metadataProps = Map(
      PROP_NAME.NAME -> Value.Str("TestName"),
      PROP_NAME.DESCRIPTION -> Value.Str("TestDescription")
    )

    val metadataParams = metadataProps.map:
      case (k, v) => k -> v.toParam

    val metadata = MapMetadata(Some(Name("TestName")), Some(Description("TestDescription")))

    val node = Node(
      "1",
      Set(ROOT_LABEL),
      Map(
        PROP_NAME.NAME -> Value.Str("TestName"),
        PROP_NAME.DESCRIPTION -> Value.Str("TestDescription")
      )
    )

  "toQueryParam" should:
    "return correct properties map for metadata" in newCase[CaseData]: data =>
      data.metadata.toQueryParams[IO]
        .logValue
        .asserting(_ mustEqual data.metadataParams)

  "fromProperties" should:
    "create metadata from properties" in newCase[CaseData]: data =>
      MapMetadata.fromProperties[IO](data.metadataProps)
        .logValue
        .asserting(_ mustEqual data.metadata)

    "fromNode should create Metadata from a valid root node" in newCase[CaseData]: data =>
      MapMetadata
        .fromNode[IO](data.node)
        .logValue
        .asserting(_ mustEqual MapMetadata("TestName", "TestDescription"))
