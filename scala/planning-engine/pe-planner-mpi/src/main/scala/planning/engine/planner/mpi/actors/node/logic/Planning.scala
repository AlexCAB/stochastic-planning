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
import planning.engine.planner.mpi.actors.node.data.Message.{
  Inference,
  LinkActivation,
  Planning as PlanningMsg,
  ThenActivation,
}

private[node] trait Planning:
  self: Actor.type =>

  private[node] def doLinkActivation[F[_]: S](msg: LinkActivation, state: St)(using d: Def, ctx: Ctx): F[St] =
    state.pure

  private[node] def doThenActivation[F[_]: S](msg: ThenActivation, state: St)(using d: Def, ctx: Ctx): F[St] =
    state.pure

  private[node] def doInference[F[_]: S](msg: Inference, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure

  private[node] def doPlanning[F[_]: S](msg: PlanningMsg, state: St)(using d: Def, ctx: Ctx): F[St] = state.pure
