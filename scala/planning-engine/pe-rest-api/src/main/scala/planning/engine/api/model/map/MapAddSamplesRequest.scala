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

package planning.engine.api.model.map

import io.circe.{Decoder, Encoder}
import planning.engine.api.model.map.payload.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.HnName

final case class MapAddSamplesRequest(
    samples: List[NewSampleData],
    hiddenNodes: List[HiddenNodeDef], // This is a list of hidden nodes used in the samples, should be unique
) extends Validation:
  lazy val hnNames: List[HnName] = hiddenNodes.map(_.name)
  private lazy val hnNamesSet = hnNames.toSet

  lazy val validations: (String, List[Throwable]) = validate("MapAddSamplesRequest")(
    (hiddenNodes.map(_.name).distinct.size == hiddenNodes.size) -> "Hidden nodes names must be unique",
    hiddenNodes.nonEmpty -> "Hidden nodes names must not be empty",
    hnNamesSet.containsAllOf(samples.flatMap(_.edgesHnNames), "Sample edges must reference only provided"),
  )

object MapAddSamplesRequest:
  import io.circe.generic.semiauto.*

  implicit val decoder: Decoder[MapAddSamplesRequest] = deriveDecoder[MapAddSamplesRequest]
  implicit val encoder: Encoder[MapAddSamplesRequest] = deriveEncoder[MapAddSamplesRequest]
