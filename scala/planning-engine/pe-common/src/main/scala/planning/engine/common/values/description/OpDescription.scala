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

package planning.engine.common.values.description

final case class OpDescription(value: Option[String]) extends AnyVal:
  override def toString: String = value.getOrElse("---")

object OpDescription:
  def fromString(value: String): OpDescription = OpDescription(if value.nonEmpty then Option(value) else None)
  def fromOption(value: Option[String]): OpDescription = value.map(fromString).getOrElse(OpDescription(None))
  def empty: OpDescription = OpDescription(None)
