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

//import cats.syntax.all.*
//import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.manager.data.Message

private[manager] trait Edges:
  self: Actor.type =>
  import Message.*

//  private[manager] def doUpsertEdges[F[_]: S](msg: UpsertEdges, state: St)(using d: Def, ctx: Ctx): F[St] =
//    def sendAddEdgeSrc(key: MeKey): F[Unit] =
//      for
//        srcRef <- state.getRef(key.src)
//        trgRef <- state.getRef(key.trg)
//        _ = ??? // TODO: Add check if SampleId indexies in samples collection.
//        _ <- logInfo(s"[UpsertEdges] found refs for $key: srcRef = $srcRef, trgRef = $trgRef")
////        _ <- srcRef.addEdgeSrc[F](MeRef(key, srcRef, trgRef), data)
//      yield ()
//
//    for
//      _ <- msg.data.edges.toList.traverse(sendAddEdgeSrc)
//      _ <- logInfo(s"[UpsertEdges] Upserted ${msg.data.edges.size} edges.")
//      _ <- d.visualizer.edgesAdded[F](msg.data.edges.keySet)
//      _ <- msg.reply(EdgesUpserted(msg.data.edges.keySet))
//    yield state

  private[manager] def doAddEdge[F[_]: S](msg: AddEdge, state: St)(using d: Def, ctx: Ctx): F[St] = ???
