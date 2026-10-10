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

private[node] trait InferenceAndPlanning:
  self: Actor.type =>
  import Message.*


  private[node] def doInference[F[_] : S](msg: Inference, state: St)(using d: Def, ctx: Ctx): F[St] =
    // TODO
    state.pure

  private[node] def doPlanExtend[F[_] : S](msg: PlanExtend, state: St)(using d: Def, ctx: Ctx): F[St] =
    // TODO
    state.pure
