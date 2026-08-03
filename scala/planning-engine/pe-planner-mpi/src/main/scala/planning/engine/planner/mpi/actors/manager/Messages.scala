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
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.message.ReplyTo
import planning.engine.planner.mpi.common.repr.Representable

private[manager] trait Messages:
  sealed trait Message extends Representable

  sealed trait NodeMessage extends Message with ReplyTo[ManagerAdaptor.Msg]

  sealed trait EdgeMessage extends Message with ReplyTo[ManagerAdaptor.Msg]:
    def keys: Set[MeKey]

  // Add nodes command. Create new MnId and add node to the graph for each node in the kit. Reply with NodeData.
  final case class AddNodes(data: NodeData.Kit, replyTo: ManagerAdaptor.Ref) extends NodeMessage

  // Upsert nodes by name command:
  // - If the node with given name already exists, returns its MnId.
  // - If node does not exist, creates and add new node.
  // Will fail if:
  // - In map found duplicate names.
  // - NodeData.Kit contains NodeData with undefined name field.
  // - Node type of found node does not match the type given in NodeData.
  final case class UpsertNodesByName(data: NodeData.Kit, replyTo: ManagerAdaptor.Ref) extends NodeMessage

  // Upsert edges command:
  // - If the edge exists, join indexies to already existing (only `AddEdgeSrc` used).
  // - If edge does not exist, creates it by adding edge source along with data to source node,
  //   and edge target to target node (`AddEdgeSrc` and `AddEdgeTrg` used).
  // Will fail if:
  // - In indexies map found duplicate SampleId (SampleId is unique per edge).
  // (!) Note: This command not have rollback mechanism, so in case of failure, some edges may be added, some not.
  final case class UpsertEdges(data: EdgeData.Kit, replyTo: ManagerAdaptor.Ref) extends EdgeMessage:
    override def keys: Set[MeKey] = data.edges.keySet

  // Sent from NodeActor to ManagerActor in case of any error.
  final case class NodeActorError(nodeRef: NodeActor.Ref, msg: Option[Representable], err: Throwable) extends Message