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

package planning.engine.map.hidden.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import planning.engine.common.values.{HiddenNodeId, OpName}
import planning.engine.map.hidden.state.node.{InitState, NodeState}

class AbstractNode[F[_]: MonadThrow](
    val id: HiddenNodeId,
    val name: OpName,
    state: AtomicCell[F, NodeState]
) extends HiddenNode[F]:

  override def toString: String = s"AbstractHiddenNode(id=$id, name=$name)"

object AbstractNode:
  def apply[F[_]: Concurrent](id: HiddenNodeId, name: OpName): F[AbstractNode[F]] =
    for
      state <- AtomicCell[F].of[NodeState](InitState)
      node = new AbstractNode[F](id, name, state)
    yield node
