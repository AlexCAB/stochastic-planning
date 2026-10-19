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
| created: 2026-10-15 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.parts

import planning.engine.common.values.io.IoTime

// Step is the vertex of the plan tree.
// Plan tree can be decomposed into set of plan paths, where each path is a sequence of steps.
// Each path is a basically a timeline, which can be split into three parts:
// - Occurred steps, is the actions or observations (events) that already happened in the past.
// - Active step, the present events (observed by the agent now).
// - Next step, is the events that expected to happen exactly the next.
// - Planned steps, is the events that are possibly (or planned to) happen in the future (i.e. after the next step).
private[node] sealed trait Step:
  def time: IoTime
  def struct: PlanStruct

private[node] object Step:
  final case class Occurred(time: IoTime, struct: PlanStruct) extends Step

  object Occurred:
    def init(time: IoTime): Occurred = Occurred(time, PlanStruct.init)

  final case class Active(time: IoTime, struct: PlanStruct) extends Step

  object Active:
    def init(time: IoTime): Active = Active(time, PlanStruct.init)

  final case class Next(time: IoTime, struct: PlanStruct) extends Step:
    def active: Active = Active(time, struct)

  object Next:
    def init(time: IoTime): Next = Next(time, PlanStruct.init)

  final case class Planned(time: IoTime, struct: PlanStruct) extends Step

  object Planned:
    def init(time: IoTime): Planned = Planned(time, PlanStruct.init)
