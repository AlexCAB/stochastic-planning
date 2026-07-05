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

package planning.engine.planner.mpi.actors.node

import planning.engine.planner.mpi.actors.ReplyTo
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.data.edge.{EdgeData, MeRef}

private[node] trait Messages:
  sealed trait Message

  sealed trait AddEdge extends Message with ReplyTo[ManagerAdaptor.Msg]:
    def ref: MeRef

  final case class AddEdgeSrc(ref: MeRef, data: EdgeData, replyTo: ManagerAdaptor.Ref) extends AddEdge

  final case class AddEdgeTrg(ref: MeRef, replyTo: ManagerAdaptor.Ref) extends AddEdge
