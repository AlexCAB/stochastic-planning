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

package planning.engine.planner.mpi.actors.manager.data

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.repr.Representable

private[manager] sealed trait Message extends Representable

private[manager] object Message:

  // Synchronous command sent to ManagerActor. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command extends Message
  sealed trait Result

  final case class AddNodes(data: NodeData.Kit, sender: ActorRef[NodesAdded]) extends Command
  final case class NodesAdded(ids: Map[MnId, Option[HnName]]) extends Result

  final case class UpsertNodesByName(data: NodeData.Kit, sender: ActorRef[NodesUpserted]) extends Command
  final case class NodesUpserted(ids: Map[MnId, Option[HnName]]) extends Result

  final case class UpsertEdges(data: EdgeData.Kit, sender: ActorRef[EdgesUpserted]) extends Command
  final case class EdgesUpserted(keys: Set[MeKey]) extends Result

  // Sent from NodeActor to ManagerActor in case of any error.
  final case class NodeActorError(nodeRef: Node, msg: Option[Representable], err: Throwable) extends Message
