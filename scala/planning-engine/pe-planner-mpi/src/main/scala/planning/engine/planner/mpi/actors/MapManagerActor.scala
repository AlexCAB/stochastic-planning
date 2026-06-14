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
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import planning.engine.planner.mpi.model.messages.{AdaptorMsg, ManagerMsg}
import planning.engine.planner.mpi.model.states.ManagerState

object MapManagerActor:
  import ManagerMsg.*, AdaptorMsg.*

  private type Ctx = ActorContext[ManagerMsg]
  private type St = ManagerState
  private type Bh = Behavior[ManagerMsg]

  private[mpi] def doAddNode[F[_]: {Async, Dispatcher}](msg: AddNode, state: St, ctx: Ctx): Bh =
    val definition = msg.data.toDefinition(state.nextId)
    val node = ctx.spawn(MapNodeActor(definition), definition.id.value.toString)

    ctx.log.info(s"Adding node ${definition}")
    msg.replyTo ! NodeAdded(definition.id, msg.data.name)
    behavior(state.withNewNode(definition.id, node))

  private def behavior[F[_]: {Async, Dispatcher}](state: St): Bh = Behaviors.setup: ctx =>
    ctx.setLoggerName("map-manager-actor")

    Behaviors.receiveMessage:
      case msg: AddNode    => doAddNode(msg, state, ctx)
      case msg: UpsertEdge => ???

  def apply[F[_]: {Async, Dispatcher}]: Behavior[ManagerMsg] = behavior[F](ManagerState.init)
