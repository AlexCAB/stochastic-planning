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

import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.Behavior
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.model.MapNode

object MapNodeActor:
  final case class State(
      id: MnId,
      data: MapNode.Data
  )

  sealed trait Message

  private def behavior(state: State): Behavior[Message] = Behaviors.setup: ctx =>
    ctx.setLoggerName(s"${state.id.reprNode}_${state.data.name.repr}")

    Behaviors.receiveMessage:
      case m: Message => behavior(state)

  def apply(id: MnId, data: MapNode.Data): Behavior[Message] = behavior(State(id, data))
