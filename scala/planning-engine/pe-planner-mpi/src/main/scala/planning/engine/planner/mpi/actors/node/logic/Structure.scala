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
| created: 05.07.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.logic

import cats.syntax.all.*
import planning.engine.planner.mpi.actors.node.data.Message.{UpsertEdgeSrc, UpsertEdgeTrg}

private[node] trait Structure:
  self: Actor.type =>

  private[node] def doUpsertEdgeSrc[F[_]: S](msg: UpsertEdgeSrc, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      newState <- state.upsertEdgeSrc(msg.ref, msg.props)
      _ <- logInfo(s"[AddEdgeSrc] Added outgoing edge of ref = ${msg.ref}")
      _ <- msg.ref.trgNode.upsertEdgeTrg[F](msg.ref, msg.props)
    yield newState

  private[node] def doUpsertEdgeTrg[F[_]: S](msg: UpsertEdgeTrg, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      newState <- state.upsertEdgeTrg(msg.ref, msg.props)
      _ <- logInfo(s"[AddEdgeTrg] Added incoming edge from ref = ${msg.ref}")
    yield newState
