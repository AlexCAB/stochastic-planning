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
| created: 2025-07-08 |||||||||||*/

package planning.engine.api.model.map.payload

import planning.engine.common.values.text.{Description, Name}
import io.circe.{Decoder, Encoder}
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.HnName

final case class NewSampleData(
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description],
    edges: List[NewSampleEdge]
) extends Validation:

  lazy val edgesHnNames: Set[HnName] = edges.toSet.flatMap(e => Set(e.sourceHnName, e.targetHnName))
  lazy val validationName: String = s"NewSampleData(name = ${name.getOrElse("None")})"

  lazy val validationErrors: List[Throwable] = validations(
    (probabilityCount > 0) -> "Probability count must be greater than zero",
    (edges.distinct == edges) -> "Edges must be unique",
    edges.nonEmpty -> "Edges must not be empty"
  )

object NewSampleData:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.json.values.*

  implicit val decoder: Decoder[NewSampleData] = deriveDecoder[NewSampleData]
  implicit val encoder: Encoder[NewSampleData] = deriveEncoder[NewSampleData]
