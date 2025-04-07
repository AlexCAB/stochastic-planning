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
| created: 2025-04-06 |||||||||||*/

package planning.engine.core.map.hidden.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import planning.engine.core.map.hidden.state.{InitNodeState, NodeState}
import cats.syntax.all.*

class AbstractHiddenNode[F[_]: MonadThrow](protected val state: AtomicCell[F, NodeState[F]]) extends HiddenNode[F]

object AbstractHiddenNode:
  def apply[F[_]: Concurrent](): F[AbstractHiddenNode[F]] =
    for
      state <- AtomicCell[F].of[NodeState[F]](InitNodeState())
      node = new AbstractHiddenNode[F](state)
    yield node
