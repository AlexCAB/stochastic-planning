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
| created: 27.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.syntax.all.*
import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.data.edge.MeRef

trait ManageEdges:
  self: ManagerActor.type =>

  private[manager] def doUpsertEdge[F[_]: S](msg: UpsertEdges, state: St)(using d: Def, ctx: Ctx): F[St] =
    for
      srcRef <- state.getRef(msg.key.src)
      trgRef <- state.getRef(msg.key.trg)
      _ <- logInfo(s"[UpsertEdges] found refs: srcRef = $srcRef, trgRef = $trgRef")
      _ = srcRef.ref ! NodeActor.AddEdgeSrc(MeRef(msg.key, srcRef, trgRef), msg.data, msg.replyTo)
    yield state
