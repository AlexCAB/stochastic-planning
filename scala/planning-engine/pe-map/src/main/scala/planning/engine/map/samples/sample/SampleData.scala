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

package planning.engine.map.samples.sample

import planning.engine.common.values.name.OpName
import planning.engine.common.values.sample.SampleId

final case class SampleData(
    id: SampleId,
    probabilityCount: Long,
    utility: Double,
    name: OpName,
    description: Option[String]
):
  override def toString: String =
    s"SampleData(id=$id, probabilityCount=$probabilityCount, utility=$utility, name=$name, description=$description)"
