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
| created: 2025-12-04 |||||||||||*/

package planning.engine.common.values.node

import planning.engine.common.values.{StringBuilders, StringVal}

final case class HnName(value: String) extends AnyVal with StringVal

object HnName extends StringBuilders[HnName]:
  protected def makeValue(str: String): HnName = HnName(str)
