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
| created: 30-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import planning.engine.planner.mpi.common.repr.Representable

private[node] final case class PlanState() extends Representable

private[node] object PlanState:
  val init = PlanState()
