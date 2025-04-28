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

import planning.engine.common.values.{OpName, SampleId}

case class SampleData(
                       id: SampleId,
                       probabilityCount: Long,
                       utility: Double,
                       name: OpName,
                       description: Option[String]
):
  override def toString: String =
    s"SampleData(id=$id, probabilityCount=$probabilityCount, utility=$utility, name=$name, description=$description)"
