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
import planning.engine.common.values.Index
import planning.engine.core.map.hidden.state.{InitNodeState, NodeState}
import planning.engine.core.map.io.node.IoNode
import cats.syntax.all.*

class ConcreteHiddenNode[F[_]: MonadThrow](
    protected val state: AtomicCell[F, NodeState[F]],
    val valueIndex: Index,
    value: Any, // Used only for visualisation
    ioNode: IoNode[F]
) extends HiddenNode[F]

object ConcreteHiddenNode:
  def apply[F[_]: Concurrent](valueIndex: Index, ioNode: IoNode[F]): F[ConcreteHiddenNode[F]] =
    for
      value <- ioNode.variable.valueForIndex(valueIndex)
      state <- AtomicCell[F].of[NodeState[F]](InitNodeState())
      node = new ConcreteHiddenNode[F](state, valueIndex, value, ioNode)
      _ <- ioNode.addConcreteNode(node)
    yield node
