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
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.common.errors.*

trait Structure:
  self: NodeActor.type =>

  private[node] def doAddEdgeSrc[F[_]: S](msg: AddEdgeSrc, state: St)(using d: Def, ctx: Ctx): F[St] = 
    for
      _ <- ctx.self.assertEqual(msg.ref.src, "AddEdgeSrc message source does not match this actor ID")
      newState <- state.addEdgeSrc(msg.ref, msg.data)
      _ <- logInfo(s"[AddEdgeSrc] Added outgoing edge to ref = ${msg.ref}")
      _ = msg.ref.trg ! NodeActor.AddEdgeTrg(msg.ref)
    yield newState

  private[node] def doAddEdgeTrg[F[_]: S](msg: AddEdgeTrg, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      _ <- ctx.self.assertEqual(msg.ref.trg, "AddEdgeTrg message target does not match this actor ID")
      newState <- state.addEdgeTrg(msg.ref)
      _ <- logInfo(s"[AddEdgeTrg] Added incoming edge from ref = ${msg.ref}")
    yield newState
