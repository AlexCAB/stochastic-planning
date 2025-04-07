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
import planning.engine.core.map.hidden.state.node.{InitNodeState, NodeState}
import cats.syntax.all.*
import planning.engine.common.values.{Name, Neo4jId}

class AbstractHiddenNode[F[_]: MonadThrow](
    protected val state: AtomicCell[F, NodeState],
    val neo4jId: Neo4jId,
    val name: Name
) extends HiddenNode[F]:

  override def toString: String = s"AbstractHiddenNode(neo4jId=$neo4jId, name=$name)"

object AbstractHiddenNode:
  def apply[F[_]: Concurrent](neo4jId: Neo4jId, name: Name): F[AbstractHiddenNode[F]] =
    for
      state <- AtomicCell[F].of[NodeState](InitNodeState)
      node = new AbstractHiddenNode[F](state, neo4jId, name)
    yield node
