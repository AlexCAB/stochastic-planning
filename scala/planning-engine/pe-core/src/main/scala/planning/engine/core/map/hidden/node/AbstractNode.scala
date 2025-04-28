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
import planning.engine.core.map.hidden.state.node.{InitState, NodeState}
import cats.syntax.all.*
import planning.engine.common.values.{OpName, Neo4jId}

class AbstractNode[F[_]: MonadThrow](
    protected val state: AtomicCell[F, NodeState],
    val neo4jId: Neo4jId,
    val name: OpName
) extends HiddenNode[F]:

  override def toString: String = s"AbstractHiddenNode(neo4jId=$neo4jId, name=$name)"

object AbstractNode:
  def apply[F[_]: Concurrent](neo4jId: Neo4jId, name: OpName): F[AbstractNode[F]] =
    for
      state <- AtomicCell[F].of[NodeState](InitState)
      node = new AbstractNode[F](state, neo4jId, name)
    yield node
