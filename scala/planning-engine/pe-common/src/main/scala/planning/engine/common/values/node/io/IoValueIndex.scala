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
| created: 2025-03-18 |||||||||||*/

package planning.engine.common.values.node.io

import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*

// IoValueIndex is used to identify the value in IoNode.
final case class IoValueIndex(value: Long) extends AnyVal:
  def toDbParam: Param = value.toDbParam
