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

import cats.effect.kernel.Concurrent
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import planning.engine.api.model.map.payload.NewSampleData
import planning.engine.api.model.values.*
import planning.engine.api.model.enums.*

final case class MapAddSamplesRequest(
    samples: List[NewSampleData]
)

object MapAddSamplesRequest:
  import io.circe.generic.auto.*

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, MapAddSamplesRequest] = jsonOf[F, MapAddSamplesRequest]
