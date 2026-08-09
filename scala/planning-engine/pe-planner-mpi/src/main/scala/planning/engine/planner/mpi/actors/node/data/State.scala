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
| created: 09-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.repr.Representable

private[node] final case class State(
    // Map of outgoing edges: target node ID -> (target node facade, edge data)
    outgoing: Map[MnId, (Node, EdgeData)],

    // Map of incoming edges: source node ID -> source node facade
    incoming: Map[MnId, Node],
) extends Representable:

  def addEdgeSrc[F[_]: MonadThrow](newRef: MeRef, newData: EdgeData): F[State] =
    def joinedData(newRef: MeRef, newData: EdgeData): F[EdgeData] = outgoing.get(newRef.key.trg) match
      case Some((trgRef, data)) if trgRef == newRef.trg => data.join(newData)
      case Some((trgRef, _)) => s"Edge target reference mismatch: expected $trgRef, got ${newRef.trg}".assertionError
      case None              => newData.pure

    joinedData(newRef, newData).map(data => copy(outgoing = outgoing + (newRef.key.trg -> (newRef.trg, data))))

  def addEdgeTrg[F[_]: MonadThrow](newRef: MeRef): F[State] = incoming.get(newRef.key.src) match
    case Some(ref) if ref == newRef.src => this.pure // Edge already exists, no change needed
    case Some(ref) => s"Ref not match for ${newRef.key.src}: expected $ref, got ${newRef.src}".assertionError
    case None      => copy(incoming = incoming + (newRef.key.src -> newRef.src)).pure

private[node] object State:
  val init = State(Map.empty, Map.empty)
