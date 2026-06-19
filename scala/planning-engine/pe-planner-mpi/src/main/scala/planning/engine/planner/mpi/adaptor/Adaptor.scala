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

package planning.engine.planner.mpi.adaptor

import org.apache.pekko.actor.typed.ActorRef

// Adaptor for connecting Cats Effect-based code with Pekko actors.
// Use Ask pattern to send messages to actors and receive responses in an effectful way.
trait AdaptorLike

class Adaptor extends AdaptorLike

object Adaptor extends Messages:
  type Msg = Message
  type Ref = ActorRef[Msg]
  
  def apply(): Adaptor = new Adaptor()
