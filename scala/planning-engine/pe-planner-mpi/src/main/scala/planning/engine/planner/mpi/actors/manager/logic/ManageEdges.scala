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
import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.common.actor.ActorRefEx.send
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}

trait ManageEdges:
  self: Actor.type =>

  private[manager] def doUpsertEdges[F[_]: S](msg: UpsertEdges, state: St)(using d: Def, ctx: Ctx): F[St] =
    def sendAddEdgeSrc(key: MeKey, data: EdgeData): F[Unit] =
      for
        srcRef <- state.getRef(key.src)
        trgRef <- state.getRef(key.trg)
        _ <- logInfo(s"[UpsertEdges] found refs for $key: srcRef = $srcRef, trgRef = $trgRef")
        _ <- srcRef.send(NodeActor.AddEdgeSrc(MeRef(key, srcRef, trgRef), data))
      yield ()

    for
      _ <- msg.data.edges.toList.traverse(sendAddEdgeSrc)
      _ <- logInfo(s"[UpsertEdges] Upserted ${msg.data.edges.size} edges.")
      _ <- d.visualizer.send(VisualizerActor.Structure.Edges.Added(msg.data.edges.keySet))
      _ <- msg.replay(ManagerAdaptor.EdgesUpserted(msg.data.edges.keySet))
    yield state
