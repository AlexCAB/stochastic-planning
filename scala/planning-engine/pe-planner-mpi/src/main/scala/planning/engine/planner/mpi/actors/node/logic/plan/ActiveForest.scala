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
import planning.engine.common.graph.inference.PU
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.actors.node.logic.Actor

private[node] trait ActiveForest:
  self: Actor.type =>
  import Message.*

  private def calcPu[F[_]: S](msg: Activation, state: St): F[PU] =
    // TODO
    PU.zero.pure

  private[node] def doActivation[F[_]: S](msg: Activation, state: St)(using d: Def, ctx: Ctx): F[St] =

    // TODO
    state.pure
