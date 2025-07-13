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
import io.circe.{Encoder, Decoder}
import planning.engine.common.validation.Validation

final case class NewSampleData(
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description],
    hiddenNodes: List[HiddenNodeDef],
    edges: List[NewSampleEdge]
) extends Validation:
  lazy val validationName: String = "NewSampleData"

  lazy val validationErrors: List[Throwable] = validations(
    (probabilityCount > 0) -> "Probability count must be greater than zero",
    (hiddenNodes.map(_.name).distinct.size == hiddenNodes.size) -> "Hidden nodes names must be unique",
    hiddenNodes.nonEmpty -> "Hidden nodes names must not be empty",
    (edges.distinct == edges) -> "Edges must be unique",
    edges.nonEmpty -> "Edges must not be empty",
    (edges.flatMap(e => Set(e.sourceHnName, e.targetHnName)) == hiddenNodes.map(_.name))
      -> "Edges must reference all provided hnNames"
  )

object NewSampleData:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.values.*

  implicit val decoder: Decoder[NewSampleData] = deriveDecoder[NewSampleData]
  implicit val encoder: Encoder[NewSampleData] = deriveEncoder[NewSampleData]
