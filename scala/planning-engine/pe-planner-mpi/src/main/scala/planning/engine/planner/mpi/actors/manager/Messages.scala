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
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.data.edge.EdgeData
import planning.engine.planner.mpi.data.node.NodeData

private[manager] trait Messages:
  sealed trait Message extends ReplyTo[ManagerAdaptor.Msg]

  // Add node command. Create new MnId and add node to the graph. Reply with NodeData.
  final case class AddNodes(data: NodeData.Kit, replyTo: ManagerAdaptor.Ref) extends Message

  // Add node by name command: 
  // - If the node with given name already exists, returns its MnId.
  // - If node does not exist, creates and add new node.
  // Will fail if:
  // - In map found duplicate names.
  // - NodeData.Kit contains NodeData with undefined name field.
  // - Node type of found node does not match the type given in NodeData.
  final case class UpsertNodesByName(data: NodeData.Kit, replyTo: ManagerAdaptor.Ref) extends Message

  final case class UpsertEdges(key: MeKey, data: EdgeData, replyTo: ManagerAdaptor.Ref) extends Message
