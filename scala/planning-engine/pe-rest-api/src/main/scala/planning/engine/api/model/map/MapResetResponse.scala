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
| created: 2025-07-24 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.Async
import cats.syntax.all.*
import io.circe.{Decoder, Encoder}
import planning.engine.common.values.db.DbName
import planning.engine.common.values.text.Name

final case class MapResetResponse(
    prevDbName: Option[DbName],
    prevMapName: Option[Name]
)

object MapResetResponse:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.values.*

  implicit val decoder: Decoder[MapResetResponse] = deriveDecoder[MapResetResponse]
  implicit val encoder: Encoder[MapResetResponse] = deriveEncoder[MapResetResponse]

  def emptyInMem[F[_]: Async]: F[MapResetResponse] = MapResetResponse(Some(DbName("in-mem")), None).pure[F]
