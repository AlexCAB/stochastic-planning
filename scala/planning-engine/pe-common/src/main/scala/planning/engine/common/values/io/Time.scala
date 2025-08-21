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

final case class Time(value: Long) extends AnyVal with LongVal:
  def increase: Time = Time(value + 1L)

object Time:
  val init: Time = Time(0L)
