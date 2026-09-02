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
| created: 30.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.node.data.state

import planning.engine.planner.mpi.common.repr.Representable

private[node] final case class Plan(v1: Int, v2: Int) extends Representable

private[node] object Plan:
  val init = Plan(0, 0)
