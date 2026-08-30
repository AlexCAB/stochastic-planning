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
import planning.engine.planner.mpi.actors.ActorBase.WithSender
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.repr.Representable

private[planner] sealed trait Message extends Representable

private[planner] object Message:

  // Synchronous command sent to Planner. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result

  // Sent by a client to compute the next action for the given observation.
  final case class Step(observation: Observation, sender: ActorRef[StepDone]) extends Command[StepDone]
  final case class StepDone(action: Action) extends Result

  // Sent from ManagerActor after a new concrete node was added to the map network.
  final case class ConNodeAdded(node: Node) extends Message
