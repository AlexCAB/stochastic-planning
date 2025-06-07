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

class MapMetadataSpec extends UnitSpecIO:

  private class CaseData extends Case:
    lazy val metadataProps = Map(
      PROP_NAME.NAME -> Value.Str("TestName"),
      PROP_NAME.DESCRIPTION -> Value.Str("TestDescription")
    )

    lazy val metadataParams = metadataProps.map((k, v) => k -> v.toParam)
    lazy val metadata = MapMetadata(Some(Name("TestName")), Some(Description("TestDescription")))
    lazy val rootNode = Node("1", Set(ROOT_LABEL.s), metadataProps)

  "toQueryParam" should:
    "return correct properties map for metadata" in newCase[CaseData]: data =>
      data.metadata.toQueryParams[IO]
        .logValue
        .asserting(_ mustEqual data.metadataParams)

  "fromProperties" should:
    "fromNode should create Metadata from a valid root node" in newCase[CaseData]: data =>
      MapMetadata
        .fromNode[IO](data.rootNode)
        .logValue
        .asserting(_ mustEqual data.metadata)
