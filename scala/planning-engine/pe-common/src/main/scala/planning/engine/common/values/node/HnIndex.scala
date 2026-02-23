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
| created: 2025-05-12 |||||||||||*/

package planning.engine.common.values.node

import planning.engine.common.values.LongVal

// Hidden node index is value (and the index of this value at the same time)
// of hidden variable (variable represented by hidden node).
final case class HnIndex(value: Long) extends AnyVal with LongVal:
  def increase: HnIndex = HnIndex(value + 1L)

  override def toString: String = s"i=$value"

object HnIndex:
  val init: HnIndex = HnIndex(1L)
