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

package planning.engine.planner.mpi.actors.node.logic

import cats.syntax.all.*
import planning.engine.planner.mpi.actors.node.data.Message

private[node] trait Planning:
  self: Actor.type =>
  import Message.*

  private[node] def doActivation[F[_]: S](msg: Activation, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doContextExtend[F[_]: S](msg: ContextExtend, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doContextShrink[F[_]: S](msg: ContextShrink, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doPathCut[F[_]: S](msg: PathCut, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doTreeCut[F[_]: S](msg: TreeCut, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doInference[F[_]: S](msg: Inference, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doPlanExtend[F[_]: S](msg: PlanExtend, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure
