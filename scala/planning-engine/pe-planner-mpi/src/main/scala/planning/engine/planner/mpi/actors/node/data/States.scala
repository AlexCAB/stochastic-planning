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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.MonadThrow
import cats.syntax.all.*
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.errors.*
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.repr.Representable

private[node] trait States:
  private[node] final case class State(
      // Map of outgoing edges: target node ID -> (target node actor reference, edge data)
      outgoing: Map[MnId, (ActorRef[NodeActor.Message], EdgeData)],

      // Map of incoming edges: source node ID -> source node actor reference
      incoming: Map[MnId, ActorRef[NodeActor.Message]],
  ) extends Representable:
    private def joinedData[F[_]: MonadThrow](newRef: MeRef, newData: EdgeData): F[EdgeData] =
      outgoing.get(newRef.key.trg) match
        case Some((trgRef, data)) if trgRef == newRef.trg => data.join(newData)
        case Some((trgRef, _)) => s"Edge target reference mismatch: expected $trgRef, got ${newRef.trg}".assertionError
        case None              => newData.pure

    def addEdgeSrc[F[_]: MonadThrow](newRef: MeRef, newData: EdgeData): F[State] =
      joinedData(newRef, newData).map(data => copy(outgoing = outgoing + (newRef.key.trg -> (newRef.trg, data))))

    def addEdgeTrg[F[_]: MonadThrow](newRef: MeRef): F[State] = incoming.get(newRef.key.src) match
      case Some(ref) if ref == newRef.src => this.pure // Edge already exists, no change needed
      case Some(ref) => s"Ref not match for ${newRef.key.src}: expected $ref, got ${newRef.src}".assertionError
      case None      => copy(incoming = incoming + (newRef.key.src -> newRef.src)).pure

  private[node] object State:
    val init = State(Map.empty, Map.empty)
