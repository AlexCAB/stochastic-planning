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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.kernel.Concurrent
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

final case class MapDefinitionRequest(
    name: String
)

object MapDefinitionRequest:
  import io.circe.generic.auto.*
  
  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, MapDefinitionRequest] = jsonOf[F, MapDefinitionRequest]
