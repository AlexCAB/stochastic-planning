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
| created: 2026-02-03 |||||||||||*/

package planning.engine.common.values.edges

import planning.engine.common.values.node.HnId

sealed trait End:
  def id: HnId

object End:
  final case class Link(id: HnId) extends End
  final case class Then(id: HnId) extends End
