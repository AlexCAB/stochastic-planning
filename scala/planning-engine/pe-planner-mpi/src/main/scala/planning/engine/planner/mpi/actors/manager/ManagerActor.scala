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
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.data.node.StaticActors

object ManagerActor extends ActorBase with Definitions with States with Messages:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-manager-actor"

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(name)

  private[manager] def doAddNodes[F[_]: S](msg: AddNodes, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      definitions <- msg.data.toDefinitions(state.nextId, StaticActors())
      nodeRefs = NodeActor.spawn(definitions, (bh, n) => ctx.spawn(bh, n))
      _ <- logInfo("Added nodes", nodeRefs)
      _ <- msg.replay(ManagerAdaptor.NodesAdded(definitions.map(d => d.id -> d.data.name).toMap))
      newState <- state.withNewNodes(nodeRefs)
    yield newState

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddNodes   => doAddNodes(msg, state)
    case msg: UpsertEdge => ??? // doUpsertEdge(msg, state, ctx)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] = msg match
    case msg: UpsertEdge => ??? // doUpsertEdge(msg, state, ctx)
    case msg             => ignoreError(msg, state, err)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)
