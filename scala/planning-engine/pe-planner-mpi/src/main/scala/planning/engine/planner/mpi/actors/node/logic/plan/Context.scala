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
| created: 2026-10-10 |||||||||||*/



package planning.engine.planner.mpi.actors.node.logic.plan

import cats.syntax.all.*
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.actors.node.logic.Actor

private[node] trait Context:
  self: Actor.type =>
  import Message.*

  private[node] def doContextExtend[F[_] : S](msg: ContextExtend, state: St)(using d: Def, ctx: Ctx): F[St] =
    // TODO
    state.pure

  private[node] def doContextShrink[F[_] : S](msg: ContextShrink, state: St)(using d: Def, ctx: Ctx): F[St] =
    // TODO
    state.pure
