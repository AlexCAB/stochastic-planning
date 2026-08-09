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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.actors.manager.data.{Definitions, Messages, States}
import planning.engine.planner.mpi.actors.manager.logic.{HandleError, ManageEdges, ManageNodes}

// Top-level stateful actor for the map network. I.e. parent actor for all NodeActor instances.
// It responsible for:
// - Generating unique MnId for nodes.
// - Tracking all node refs and names.
// - Handling adding/upserting nodes and edges (by delegating to the relevant NodeActor instances).
// - Response to the ManagerAdaptor `ask` queries with success or error.
// - Handling any error that happens in child actors by receiving `NodeActorError` (in simple implementation
//   just kill all system in case any error).
private[manager] object Actor extends ActorBase
    with Definitions with States with Messages with ManageNodes with ManageEdges with HandleError:

  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-manager-actor"

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(name)

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddNodes          => doAddNodes(msg, state)
    case msg: UpsertNodesByName => doUpsertNodesByName(msg, state)
    case msg: UpsertEdges       => doUpsertEdges(msg, state)
    case msg: NodeActorError    => doHandleNodeError(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    doHandleManagerError(msg, state, err)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)
