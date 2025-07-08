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
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.text.Name
import planning.engine.api.model.values.* 
import planning.engine.api.model.enums.*

final case class NewSampleEdge(
    sourceHnName: Name,
    targetHnName: Name,
    edgeType: EdgeType
)

object NewSampleEdge:
  import io.circe.generic.auto.*

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, NewSampleEdge] = jsonOf[F, NewSampleEdge]
