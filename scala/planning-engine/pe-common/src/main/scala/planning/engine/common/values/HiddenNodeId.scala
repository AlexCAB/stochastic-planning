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



package planning.engine.common.values

import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*

final case class  HiddenNodeId(value: Long) extends AnyVal:
  def toDbParam: Param = value.toDbParam
  def increase: HiddenNodeId = HiddenNodeId(value + 1L)

object HiddenNodeId:
  val init: HiddenNodeId = HiddenNodeId(1L)