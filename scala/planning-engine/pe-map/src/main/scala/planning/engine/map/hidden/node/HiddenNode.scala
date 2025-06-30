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
| created: 2025-04-05 |||||||||||*/

package planning.engine.map.hidden.node

import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import neotypes.query.QueryArg.Param

trait HiddenNode[F[_]]:
  def id: HnId
  def name: Option[Name]

  def toProperties(initNextHnIndex: Long): F[Map[String, Param]]
