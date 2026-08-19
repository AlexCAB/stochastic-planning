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
import planning.engine.planner.mpi.actors.manager.data.*
import planning.engine.planner.mpi.actors.manager.logic.{Edges, Errors, Nodes}

// Top-level stateful actor for the map network. I.e. parent actor for all NodeActor instances.
// It responsible for:
// - Generating unique MnId for nodes.
// - Tracking all node refs and names.
// - Handling adding/upserting nodes and edges (by delegating to the relevant NodeActor instances).
// - Response to the ManagerAdaptor `ask` queries with success or error.
// - Handling any error that happens in child actors by receiving `NodeActorError` (in simple implementation
//   just kill all system in case any error).
private[manager] object Actor extends ActorBase with Nodes with Edges with Samples with Errors:
  import Message.*, ActorBase.GetState

  override type Def = Definition
  override type Msg = Message | GetState[St]
  override protected type St = State

  val name = "map-manager-actor"

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(name)

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddNode        => doAddNode(msg, state)
    case msg: AddEdge        => doAddEdge(msg, state)
    case msg: AddManSamples  => doAddManSamples(msg, state)
    case msg: AddGenSamples  => doAddGenSamples(msg, state)
    case msg: NodeActorError => doHandleNodeError(msg, state)
    case msg: GetState[St]   => doGetState(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] =
    doHandleManagerError(msg, state, err)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)
