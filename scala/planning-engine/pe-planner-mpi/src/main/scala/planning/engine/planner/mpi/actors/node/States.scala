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

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.data.edge.EdgeData
import planning.engine.planner.mpi.data.node.MnRef

private[node] trait States:
  private[node] final case class State(
      // Map of outgoing edges: target node ID -> (target node actor reference, edge data)
      outgoing: Map[MnId, (ActorRef[Node.Message], EdgeData)],

      // Map of incoming edges: source node ID -> source node actor reference
      incoming: Map[MnId, ActorRef[Node.Message]],

      // Set of outgoing edges which is in process of being established
      // (i.e., connection messages sent but not yet acknowledged)
      connecting: Set[MnRef],
  )

  private[node] object State:
    val init = State(Map.empty, Map.empty, Set.empty)
