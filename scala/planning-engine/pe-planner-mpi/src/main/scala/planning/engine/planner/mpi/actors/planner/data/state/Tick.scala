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

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.io.IoTime
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.Message.TickDone
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.repr.Representable

// Is state of the plan at a specific time (tick).
// Class fields ordered by thick execution phases, see `Planner.tick` docs for more details about the execution phases.
private[planner] final case class Tick(
    // Current time marker of the plan execution (tick).
    time: IoTime,

    // Sender of the tick command, used to send back the result after all tick phases is done.
    sender: Option[ActorRef[TickDone]],

    // Next steps from the previous step, used in cleanup phase.
    prevNextSteps: Set[StepKey],

    // Active concrete nodes found by observed values.
    activeConNodes: Set[Node.Con],

    // Active steps found by tracing active forest at activation phase.
    activeSteps: Set[StepKey],

    // Next steps collected in context update phase.
    nextSteps: Set[StepKey],

    // Root steps collected in context update phase.
    rootSteps: Set[StepKey],
) extends Representable:

  def next(time: IoTime, conNodes: Set[Node.Con], sender: ActorRef[TickDone]): Tick = Tick.init
    .copy(
      time = time,
      prevNextSteps = nextSteps,
      activeConNodes = conNodes,
      sender = Some(sender),
    )

  override lazy val toString: String =
    s"""Tick(
       |  time = $time,
       |  prevNextSteps = $prevNextSteps,
       |  activeConNodes = $activeConNodes,
       |  activeSteps = $activeSteps, 
       |  nextSteps = $nextSteps,
       |  rootSteps = $rootSteps
       |)""".stripMargin

private[planner] object Tick:
  val init: Tick = Tick(IoTime(0), None, Set.empty, Set.empty, Set.empty, Set.empty, Set.empty)
