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
import planning.engine.planner.mpi.adaptors.PpiMapAdaptor.{NodeAdded, Reply}
import planning.engine.planner.mpi.model.MapNode

object MapManagerActor:
  sealed trait Command

  final case class AddNode(data: MapNode.Data, replyTo: ActorRef[Reply]) extends Command

  def apply(count: Int): Behavior[Command] = Behaviors.setup: ctx =>
    Behaviors.receiveMessage:
      case AddNode(data, sender) =>
        ctx.log.info(s"Adding node with name ${data.name}, count: $count")
        sender ! NodeAdded(MnId.Con(123), data.name)
        apply(count + 1)
