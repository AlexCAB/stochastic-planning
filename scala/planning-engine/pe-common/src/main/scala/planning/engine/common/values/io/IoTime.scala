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
| created: 2025-08-20 |||||||||||*/

package planning.engine.common.values.io

import planning.engine.common.values.LongVal

final case class IoTime(value: Long) extends AnyVal with LongVal:
  def increase: IoTime = IoTime(value + 1L)

object IoTime:
  val init: IoTime = IoTime(0L)
