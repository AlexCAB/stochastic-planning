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
| created: 12.06.2026 |||||||||||*/

package planning.engine.planner.mpi.states

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.MapNodeActor.Message

final case class ManagerState(
    nodes: Map[MnId, ActorRef[Message]],
    nextId: Long
):
  def withNewNode(id: MnId, ref: ActorRef[Message]): ManagerState = 
    copy(nodes = nodes + (id -> ref), nextId = nextId + 1)

object ManagerState:
  val init: ManagerState = ManagerState(Map.empty, 1L)
