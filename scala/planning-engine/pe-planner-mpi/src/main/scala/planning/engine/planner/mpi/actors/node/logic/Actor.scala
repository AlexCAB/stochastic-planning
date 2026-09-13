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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic

import cats.syntax.all.*
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.Stateful
import planning.engine.planner.mpi.actors.node.data.*
import planning.engine.planner.mpi.actors.node.data.State

private[node] object Actor extends Stateful with Structure:
  import Message.*, Stateful.GetState

  override type Def = Definition
  override type Msg = Message | GetState[St]

  override protected type St = State

  override protected def setup(s: St)(using d: Def, ctx: Ctx): Unit = ctx.setLoggerName(s"map-node-actor-${d.id}")

  override protected def receive[F[_]: S](msg: Msg, state: St)(using Def, Ctx): F[St] = msg match
    case msg: UpsertEdgeSrc => doUpsertEdgeSrc(msg, state)
    case msg: UpsertEdgeTrg => doUpsertEdgeTrg(msg, state)
    case msg: GetState[St]  => doGetState(msg, state)

  override protected def error[F[_]: S](msg: Msg, state: St, err: Throwable)(using d: Def, c: Ctx): F[St] =
    for
      self <- d.self
      _ <- d.actors.manager.reportError[F](self, Some(msg), err).as(state)
    yield state

  def spawn(definition: Def, make: (Behavior[Msg], String) => Ref): Ref =
    make(apply(definition, State.init), definition.id.value.toString)
