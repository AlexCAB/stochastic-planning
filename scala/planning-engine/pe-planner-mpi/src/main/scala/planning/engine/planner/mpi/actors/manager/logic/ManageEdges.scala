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
import planning.engine.planner.mpi.common.data.edge.MeRef

trait ManageEdges:
  self: ManagerActor.type =>
//    
//  private def sendAddEdgeSrc[F[_]: S](key: MeKey, data: EdgeData, state: St)(using ctx: Ctx): F[Unit] =
//    for
//      srcRef <- state.getRef(key.src)
//      trgRef <- state.getRef(key.trg)
//      _ <- logInfo(s"[UpsertEdges] found refs for $key: srcRef = $srcRef, trgRef = $trgRef")
//      _ = srcRef ! NodeActor.AddEdgeSrc(MeRef(key, srcRef, trgRef), data, msg.replyTo)
//    yield ()

  private[manager] def doUpsertEdges[F[_]: S](msg: UpsertEdges, state: St)(using d: Def, ctx: Ctx): F[St] = msg
    .data.edges.toList.traverse: (key, data) =>
      for
        srcRef <- state.getRef(key.src)
        trgRef <- state.getRef(key.trg)
        _ <- logInfo(s"[UpsertEdges] found refs for $key: srcRef = $srcRef, trgRef = $trgRef")
        _ = srcRef ! NodeActor.AddEdgeSrc(MeRef(key, srcRef, trgRef), data, msg.replyTo)
      yield ()
    .as(state)
