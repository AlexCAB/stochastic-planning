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

package planning.engine.common.values.sample

import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.values.SampleId

final case class SampleId(value: Long) extends AnyVal:
  def toDbParam: Param = value.toDbParam

object SampleId:
  val init: SampleId = SampleId(1L)
