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

package planning.engine.planner.mpi.actors.manager

import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.ReplyTo
import planning.engine.planner.mpi.adaptor.Adaptor
import planning.engine.planner.mpi.data.edge.EdgeData
import planning.engine.planner.mpi.data.node.NodeData

private[manager] trait Messages:
  sealed trait Message

  final case class AddNode(data: NodeData, replyTo: Adaptor.Ref) extends Message
      with ReplyTo[Adaptor.Msg]

  final case class UpsertEdge(key: MeKey, data: EdgeData, replyTo: Adaptor.Ref) extends Message
      with ReplyTo[Adaptor.Msg]
