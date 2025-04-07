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
| created: 2025-04-05 |||||||||||*/


package planning.engine.core.map.hidden.node

import cats.MonadThrow
import cats.effect.std.AtomicCell
import planning.engine.core.map.hidden.state.NodeState

trait HiddenNode[F[_]: MonadThrow]:
  protected val state: AtomicCell[F, NodeState[F]]
  
