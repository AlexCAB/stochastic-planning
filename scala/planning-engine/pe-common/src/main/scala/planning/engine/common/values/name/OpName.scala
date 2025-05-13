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
| created: 2025-04-08 |||||||||||*/

package planning.engine.common.values.name

import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*

final case class OpName(value: Option[String]) extends AnyVal:
  override def toString: String = value.getOrElse("---")
  def toDbParam: Option[Param] = value.map(_.toDbParam)

object OpName:
  def fromString(value: String): OpName = OpName(if value.nonEmpty then Option(value) else None)
  def fromOption(value: Option[String]): OpName = value.map(fromString).getOrElse(OpName(None))
  def empty: OpName = OpName(None)
