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

import cats.effect.kernel.Concurrent
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import planning.engine.common.values.text.{Description, Name}
import planning.engine.api.model.values.*
import planning.engine.api.model.enums.*

final case class NewSampleData(
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description],
    hnNames: List[Name],
    edges: List[NewSampleEdge]
)

object NewSampleData:
  import io.circe.generic.auto.*

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, NewSampleData] = jsonOf[F, NewSampleData]
