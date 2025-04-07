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
import planning.engine.common.values.{Name, SampleId}

case class SampleData[F[_]: MonadThrow](
    id: SampleId,
    probabilityCount: Long,
    utility: Double,
    name: Name,
    description: Option[String]
):
  override def toString: String =
    s"SampleData(id=$id, probabilityCount=$probabilityCount, utility=$utility, name=$name, description=$description)"
