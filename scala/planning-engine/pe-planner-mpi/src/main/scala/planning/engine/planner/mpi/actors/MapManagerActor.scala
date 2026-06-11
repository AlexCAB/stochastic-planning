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

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.MapNodeActor.Message
import planning.engine.planner.mpi.adaptors.PpiMapAdaptor.{NodeAdded, Reply}
import planning.engine.planner.mpi.model.MapNode

object MapManagerActor:
  final case class State(
      nodes: Map[MnId, ActorRef[Message]],
      nextId: Long
  ):
    def withNewNode(id: MnId, ref: ActorRef[Message]): State = copy(nodes = nodes + (id -> ref), nextId = nextId + 1)

  object State:
    val init: State = State(Map.empty, 1L)

  sealed trait Command

  final case class AddNode(data: MapNode.Data, replyTo: ActorRef[Reply]) extends Command

  private def behavior(state: State): Behavior[Command] = Behaviors.setup: ctx =>
    ctx.setLoggerName("map-manager-actor")

    Behaviors.receiveMessage:
      case AddNode(data, sender) =>
        val id = data.makeMnId(state.nextId)
        val node = ctx.spawn(MapNodeActor(id, data), id.value.toString)

        ctx.log.info(s"Adding node: id = $id, ${data.name.repr}")
        sender ! NodeAdded(id, data.name)
        behavior(state.withNewNode(id, node))

  def apply(): Behavior[Command] = behavior(State.init)
