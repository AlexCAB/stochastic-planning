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
| created: 2025-04-27 |||||||||||*/

package planning.engine.common.values.text

import planning.engine.common.values.StringVal

final case class Description(value: String) extends AnyVal with StringVal

object Description:
  def fromStringOptional(value: String): Option[Description] = if value.nonEmpty then Some(Description(value)) else None

  def fromOptionString(value: Option[String]): Option[Description] =
    value.flatMap(v => if v.nonEmpty then Some(Description(v)) else None)
