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
| created: 08.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors

import cats.effect.Async
import cats.effect.std.Dispatcher
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.data.MapNode.Definition
import planning.engine.planner.mpi.states.NodeState

object MapNodeActor:
  sealed trait Message
  
  private def behavior[F[_]: Async](state: NodeState)(using definition: Definition, dispatcher: Dispatcher[F]): Behavior[Message] = Behaviors.setup: ctx =>
    ctx.setLoggerName(s"${definition.id.reprNode}_${definition.data.name.repr}")

    Behaviors.receiveMessage:
      case m: Message => behavior(state)

  def apply[F[_]: Async](definition: Definition)(using dispatcher: Dispatcher[F]): Behavior[Message] = 
    given Definition = definition
    behavior(NodeState.init)