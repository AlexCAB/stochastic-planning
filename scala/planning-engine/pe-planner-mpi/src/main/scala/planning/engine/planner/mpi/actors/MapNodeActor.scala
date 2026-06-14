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
import org.apache.pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.model.data.MapNode.Definition
import planning.engine.planner.mpi.model.messages.NodeMsg
import planning.engine.planner.mpi.model.states.NodeState

object MapNodeActor:
  //import NodeMsg.*

  private type Ctx = ActorContext[NodeMsg]
  private type St = NodeState
  private type Bh = Behavior[NodeMsg]
  private type Def = Definition

  private def behavior[F[_]: {Async, Dispatcher}](state: St)(using definition: Def): Bh = Behaviors.setup: ctx =>
    ctx.setLoggerName(s"${definition.id.reprNode}_${definition.data.name.repr}")

    Behaviors.receiveMessage:
      case m: NodeMsg => behavior(state)

  def apply[F[_]: {Async, Dispatcher}](definition: Definition): Behavior[NodeMsg] =
    given Definition = definition
    behavior(NodeState.init)
