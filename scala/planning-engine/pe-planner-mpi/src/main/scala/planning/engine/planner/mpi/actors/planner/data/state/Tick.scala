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
| created: 2026-10-13 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data.state

import planning.engine.planner.mpi.repr.Representable

private[planner] final case class Tick() extends Representable

private[planner] object Tick:
  val init: Tick = Tick()
