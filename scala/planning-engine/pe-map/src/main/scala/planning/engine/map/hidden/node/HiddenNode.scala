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

import planning.engine.common.values.text.Description
import planning.engine.common.values.node.{HnId, HnName}

trait HiddenNode[F[_]]:
  def id: HnId
  def name: Option[HnName]
  def description: Option[Description]
