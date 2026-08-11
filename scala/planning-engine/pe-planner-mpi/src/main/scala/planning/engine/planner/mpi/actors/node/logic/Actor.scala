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
| created: 09-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic

import cats.syntax.all.*
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.ActorBase
import planning.engine.planner.mpi.actors.node.data.*

private[node] object Actor extends ActorBase with Structure:
  import Message.*

  override type Def = Definition
  override type Msg = Message

  override protected type St = State

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(s"map-node-actor-${d.id}")

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: AddEdgeSrc => doAddEdgeSrc(msg, state)
    case msg: AddEdgeTrg => doAddEdgeTrg(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using d: Def, c: Ctx): F[St] =
    d.actors.manager.reportError[F](d.self, Some(msg), err).as(state)

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref =
    make(apply(definition, State.init), definition.id.value.toString) 
