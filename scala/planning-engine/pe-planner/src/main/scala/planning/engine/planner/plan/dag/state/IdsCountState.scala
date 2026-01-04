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
| created: 2026-01-04 |||||||||||*/

package planning.engine.planner.plan.dag.state

import planning.engine.common.values.node.SnId

final case class IdsCountState(
    nextSnId: Long
):
  def getNextSnIds(n: Long): (IdsCountState, List[SnId]) =
    val snIds = (nextSnId until (nextSnId + n)).toList.map(SnId.apply)
    val newState = copy(nextSnId = nextSnId + n)
    (newState, snIds)

object IdsCountState:
  lazy val init: IdsCountState = IdsCountState(nextSnId = 1L)
