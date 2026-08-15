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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.repr.Representable
import planning.engine.planner.mpi.actors.ActorBase.WithSender

private[node] sealed trait Message extends Representable

private[node] object Message:

  // Synchronous command sent to Manager. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result

  sealed trait AddEdge extends Message:
    def ref: MeRef

  final case class AddEdgeSrc(ref: MeRef, data: EdgeData) extends AddEdge

  final case class AddEdgeTrg(ref: MeRef) extends AddEdge
