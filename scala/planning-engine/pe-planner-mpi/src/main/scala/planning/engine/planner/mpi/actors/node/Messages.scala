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
import planning.engine.planner.mpi.data.edge.EdgeData
import planning.engine.planner.mpi.data.node.MnRef

private[node] trait Messages:
  sealed trait Message

  final case class AddEdgeSrc(src: MnRef, trg: MnRef, data: EdgeData, replyTo: ManagerAdaptor.Ref)
      extends Message with ReplyTo[ManagerAdaptor.Msg]
