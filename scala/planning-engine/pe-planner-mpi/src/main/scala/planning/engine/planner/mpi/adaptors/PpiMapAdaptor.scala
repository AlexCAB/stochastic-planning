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

import planning.engine.common.values.node.{HnName, MnId}

// Adaptor for connecting Cats Effect-based code with Pekko actors.
// Use Ask pattern to send messages to actors and receive responses in an effectful way.
trait PpiMapAdaptorLike

class PpiMapAdaptor extends PpiMapAdaptorLike

object PpiMapAdaptor:
  sealed trait Reply
  
  final case class NodeAdded(id: MnId, name: Option[HnName]) extends Reply

  def apply(): PpiMapAdaptor = new PpiMapAdaptor()