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
| created: 10.06.2026 |||||||||||*/

package planning.engine.planner.mpi.adaptor.manager

import org.apache.pekko.actor.typed.ActorRef

// Map actor manager adaptor for connecting Cats Effect-based code with Pekko actors.
// Use Ask pattern to send messages to actors and receive responses in an effectful way.
//
// Initial implementation will be as simple as possible:
// - Use a single actor for managing the graph (nodes and edges).
// - Only add and update operations for nodes and edges will be supported.
trait ManagerAdaptorLike

class ManagerAdaptor extends ManagerAdaptorLike

object ManagerAdaptor extends Messages:
  type Msg = Message
  type Ref = ActorRef[Msg]

  def apply(): ManagerAdaptor = new ManagerAdaptor()
