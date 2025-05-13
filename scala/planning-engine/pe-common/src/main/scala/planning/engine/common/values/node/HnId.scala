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
| created: 2025-05-11 |||||||||||*/

package planning.engine.common.values.node

import planning.engine.common.values.LongVal

// Hidden Node ID is used to identify the hidden node in the graph.
final case class HnId(value: Long) extends AnyVal with LongVal:
  def increase: HnId = HnId(value + 1L)

object HnId:
  val init: HnId = HnId(1L)
