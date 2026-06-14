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
| created: 14.06.2026 |||||||||||*/

package planning.engine.planner.mpi.model.messages

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.model.data.{MapEdgeData, MapNode}
import planning.engine.planner.mpi.model.messages.AdaptorMsg.{EdgeResult, NodeResult}

sealed trait ManagerMsg

object ManagerMsg:
  final case class AddNode(data: MapNode.Data, replyTo: ActorRef[NodeResult]) extends ManagerMsg

  final case class UpsertEdge(key: MeKey, data: MapEdgeData, replyTo: ActorRef[EdgeResult]) extends ManagerMsg
