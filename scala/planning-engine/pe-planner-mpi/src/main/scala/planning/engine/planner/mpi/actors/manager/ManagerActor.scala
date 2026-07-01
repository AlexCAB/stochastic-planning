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

package planning.engine.planner.mpi.actors.manager

import cats.syntax.all.*
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.actors.manager.logic.{ManageNodes, EdgesLogic}

object ManagerActor extends ActorBase with Definitions with States with Messages with ManageNodes with EdgesLogic:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-manager-actor"

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(name)

  private[manager] def doAddNodes[F[_]: S](msg: AddNodes, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (ids, newState) <- addNodes(msg.data, state)
      _ <- logInfo("[AddNodes] added nodes", ids.view.mapValues(_.repr).toMap)
      _ <- msg.replay(ManagerAdaptor.NodesAdded(ids))
    yield newState

  private[manager] def doUpsertNodesByName[F[_]: S](msg: UpsertNodesByName, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      (ids, newState) <- upsertNodesByName(msg.data, state)
      _ <- logInfo("[UpsertNodesByName] result nodes", ids)
      _ <- msg.replay(ManagerAdaptor.NodesAdded(ids))
    yield newState

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddNodes          => doAddNodes(msg, state)
    case msg: UpsertNodesByName => doUpsertNodesByName(msg, state)
    case msg: UpsertEdges       => ??? // doUpsertEdge(msg, state, ctx)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] = msg match
    case msg: NodeMessage => msg.replay(ManagerAdaptor.NodesError(err, state.nodeRefs.keySet)).as(state)
    case msg: EdgeMessage => ??? // Create EdgeError

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)
