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
| created: 2025-07-15 |||||||||||*/

package planning.engine.common.values.db

import planning.engine.common.values.{StringBuilders, StringVal}

final case class DbName(value: String) extends AnyVal with StringVal

object DbName extends StringBuilders[DbName]:
  protected def makeValue(str: String): DbName = DbName(str)
