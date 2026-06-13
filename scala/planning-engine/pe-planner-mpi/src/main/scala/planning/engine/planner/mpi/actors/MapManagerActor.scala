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

//import cats.MonadThrow
import cats.effect.Async
import cats.effect.std.Dispatcher
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
//import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.adaptors.PpiMapAdaptor.{NodeAdded, Reply}
import planning.engine.planner.mpi.data.MapNode
import planning.engine.planner.mpi.states.ManagerState

object MapManagerActor:
  sealed trait Command

  final case class AddNode(data: MapNode.Data, replyTo: ActorRef[Reply]) extends Command

//  final case class UpsertEdge(key: MeKey, data: MapEdge.Data, replyTo: ActorRef[Reply]) extends Command

  private def behavior[F[_]: Async](state: ManagerState)(using dispatcher: Dispatcher[F]): Behavior[Command] =
    Behaviors.setup: ctx =>
      ctx.setLoggerName("map-manager-actor")

      Behaviors.receiveMessage:
        case AddNode(data, sender) =>
          val definition = data.toDefinition(state.nextId)
          val node = ctx.spawn(MapNodeActor(definition), definition.id.value.toString)

          ctx.log.info(s"Adding node ${definition}")
          sender ! NodeAdded(definition.id, data.name)
          behavior(state.withNewNode(definition.id, node))

  def apply[F[_]: Async](using dispatcher: Dispatcher[F]): Behavior[Command] =
    behavior[F](ManagerState.init)


