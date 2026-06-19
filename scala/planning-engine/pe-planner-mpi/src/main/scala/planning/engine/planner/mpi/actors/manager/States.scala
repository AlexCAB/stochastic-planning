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

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node

private[manager] trait States:
  private[manager] final case class State(
      // List of all node in map network
      nodes: Map[MnId, ActorRef[Node.Message]],

      // Next ID to assign to a new node (incremented for each new node)
      nextId: Long,
  ):
    def withNewNode(id: MnId, ref: ActorRef[Node.Message]): State =
      copy(nodes = nodes + (id -> ref), nextId = nextId + 1)

  private[manager] object State:
    val init: State = State(Map.empty, 1L)
