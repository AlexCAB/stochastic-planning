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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.planner.mpi.actors.Base.WithSender
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.repr.Representable

private[planner] sealed trait Message extends Representable

private[planner] object Message:

  // Synchronous command sent to Planner. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result

  // Sent by a client to compute the next action for the given observation.
  final case class TickRun(observation: Observation, sender: ActorRef[TickDone]) extends Command[TickDone]
  final case class TickDone(action: Action) extends Result

  // Sent from ManagerActor after a new concrete node was added to the map network.
  final case class ConNodesAdded(nodes: Set[Node.Con]) extends Message

  // Sent from the highest abstract nodes (roots of abstraction trees) with collected active steps.
  final case class AddActivePath(path: ActivePath) extends Message

  // Sent from nodes with new Next steps, generated on context extension.
  final case class NextSteps(steps: Set[StepKey]) extends Message

  // Sent from nodes with roots of sequence trees, found on context shrinking.
  final case class RootSteps(steps: Set[StepKey]) extends Message
