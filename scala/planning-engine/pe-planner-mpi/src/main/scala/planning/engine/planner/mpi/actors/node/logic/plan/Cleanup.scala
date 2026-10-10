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

private[node] trait Cleanup:
  self: Actor.type =>
  import Message.*

  private[node] def doPathCut[F[_]: S](msg: PathCut, state: St)(using d: Def, ctx: Ctx): F[St] =
    // TODO
    state.pure

  private[node] def doTreeCut[F[_]: S](msg: TreeCut, state: St)(using d: Def, ctx: Ctx): F[St] =
    // TODO
    state.pure