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
| created: 2025-05-12 |||||||||||*/

package planning.engine.common.values.node.hidden

import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*

final case class Index(value: Long) extends AnyVal:
  def toDbParam: Param = value.toDbParam
