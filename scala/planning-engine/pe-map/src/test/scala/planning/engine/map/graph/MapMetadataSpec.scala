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
import planning.engine.common.UnitSpecWithData
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.PROP
import planning.engine.common.values.db.Neo4j.ROOT_LABEL
import planning.engine.common.properties.PropertiesMapping.*
import planning.engine.common.values.text.{Description, Name}

class MapMetadataSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val metadataProps = Map(
      PROP.NAME -> Value.Str("TestName"),
      PROP.DESCRIPTION -> Value.Str("TestDescription")
    )

    lazy val metadataParams = metadataProps.map((k, v) => k -> v.toParam)
    lazy val metadata = MapMetadata(Name.some("TestName"), Description.some("TestDescription"))
    lazy val rootNode = Node("1", Set(ROOT_LABEL), metadataProps)

  "MapMetadata.toQueryParam" should:
    "return correct properties map for metadata" in newCase[CaseData]: (tn, data) =>
      data.metadata.toQueryParams[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.metadataParams)

  "MapMetadata.fromProperties(...)" should:
    "fromNode should create Metadata from a valid root node" in newCase[CaseData]: (tn, data) =>
      MapMetadata
        .fromNode[IO](data.rootNode)
        .logValue(tn)
        .asserting(_ mustEqual data.metadata)
