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
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.adaptor.Adaptor
import planning.engine.planner.mpi.data.node.StaticActors

object Manager extends ActorBase with Definitions with States with Messages:
  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  val name = "map-manager-actor"

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(name)

  private[manager] def doAddNode[F[_]: S](msg: AddNode, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      definition <- msg.data.toDefinition(state.nextId, StaticActors())
      nodeRef = Node.spawn(definition, (bh, n) => ctx.spawn(bh, n))
      _ <- logInfo(s"Added node $definition, path: ${nodeRef.path}")
      _ <- msg.replay(Adaptor.NodeAdded(definition.id, msg.data.name))
    yield state.withNewNode(definition.id, nodeRef)

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddNode    => doAddNode(msg, state)
    case msg: UpsertEdge => ??? // doUpsertEdge(msg, state, ctx)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using Def, Ctx): F[St] = msg match
    case msg: UpsertEdge => ??? // doUpsertEdge(msg, state, ctx)
    case msg             => ignoreError(msg, state, err)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref = make(apply(definition, State.init), name)
