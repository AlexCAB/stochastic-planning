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
import planning.engine.common.errors.*
import planning.engine.planner.mpi.actors.node.data.Message.{UpsertEdgeSrc, UpsertEdgeTrg}

private[node] trait Structure:
  self: Actor.type =>

  private[node] def doUpsertEdgeSrc[F[_]: S](msg: UpsertEdgeSrc, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      _ <- msg.ref.key.src.assertEquals(d.id, "Edge source does not match this node's ID")
      _ <- msg.ref.srcNode.assertEquals(d.self, "Edge source node does not match this node")
      newState <- state.mapStruct(_.upsertEdgeSrc(msg.ref, msg.props))
      _ <- logInfo(s"[AddEdgeSrc] Added outgoing edge of ref = ${msg.ref}")
      _ <- msg.ref.trgNode.upsertEdgeTrg[F](msg.ref, msg.props)
    yield newState

  private[node] def doUpsertEdgeTrg[F[_]: S](msg: UpsertEdgeTrg, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      _ <- msg.ref.key.trg.assertEquals(d.id, "Edge target does not match this node's ID")
      _ <- msg.ref.trgNode.assertEquals(d.self, "Edge target node does not match this node")
      newState <- state.mapStruct(_.upsertEdgeTrg(msg.ref, msg.props))
      _ <- logInfo(s"[AddEdgeTrg] Added incoming edge from ref = ${msg.ref}")
    yield newState
