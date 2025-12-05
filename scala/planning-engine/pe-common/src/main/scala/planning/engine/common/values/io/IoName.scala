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

package planning.engine.common.values.io

import planning.engine.common.values.{StringBuilders, StringVal}

final case class IoName(value: String) extends AnyVal with StringVal

object IoName extends StringBuilders[IoName]:
  protected def makeValue(str: String): IoName = IoName(str)
