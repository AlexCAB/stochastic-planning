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

package planning.engine.planner.mpi.adaptors

// Adaptor for connecting Cats Effect-based code with Pekko actors.
// Use Ask pattern to send messages to actors and receive responses in an effectful way.
trait PpiMapAdaptorLike

class PpiMapAdaptor extends PpiMapAdaptorLike

object PpiMapAdaptor:
  def apply(): PpiMapAdaptor = new PpiMapAdaptor()
