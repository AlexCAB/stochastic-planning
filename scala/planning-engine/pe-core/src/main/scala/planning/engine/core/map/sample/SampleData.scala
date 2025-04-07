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
| created: 2025-04-07 |||||||||||*/

package planning.engine.core.map.sample

import cats.MonadThrow
import planning.engine.common.values.SampleId

case class SampleData[F[_]: MonadThrow](
    id: SampleId,
    probabilityCount: Long,
    utility: Double,
    name: Option[String],
    description: Option[String]
)
