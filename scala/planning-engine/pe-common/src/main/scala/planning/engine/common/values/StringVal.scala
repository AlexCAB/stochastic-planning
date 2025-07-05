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
| created: 2025-05-13 |||||||||||*/

package planning.engine.common.values

import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*

trait StringVal extends Any:
  val value: String
  def toDbParam: Param = value.toDbParam

object StringVal:
  extension (strVal: Option[StringVal])
    def toStr: String = strVal.map(_.value).getOrElse("None")
