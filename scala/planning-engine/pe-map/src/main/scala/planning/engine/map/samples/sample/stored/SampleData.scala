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

package planning.engine.map.samples.sample.stored

import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}

final case class SampleData(
    id: SampleId,
    probabilityCount: Long,
    utility: Double,
    name: Option[Name],
    description: Option[Description]
):
  override def toString: String =
    s"SampleData(id=$id, probabilityCount=$probabilityCount, utility=$utility, name=$name, description=$description)"
