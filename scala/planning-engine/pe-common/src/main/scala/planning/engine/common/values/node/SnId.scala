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
| created: 2025-08-23 |||||||||||*/

package planning.engine.common.values.node

import planning.engine.common.values.LongVal

// Step Node ID in the planning DAG.
final case class SnId(value: Long) extends AnyVal with LongVal:
  def increase: SnId = SnId(value + 1L)

object SnId:
  val init: SnId = SnId(1L)
