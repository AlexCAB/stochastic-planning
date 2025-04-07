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
import planning.engine.common.values.{Index, Name, Neo4jId}
import planning.engine.core.map.hidden.state.node.{InitNodeState, NodeState}
import planning.engine.core.map.io.node.IoNode
import cats.syntax.all.*

class ConcreteHiddenNode[F[_]: MonadThrow](
    protected val state: AtomicCell[F, NodeState],
    val neo4jId: Neo4jId,
    val name: Name,
    val valueIndex: Index,
    value: Any, // Used only for visualisation
    ioNode: IoNode[F]
) extends HiddenNode[F]:

  override def toString: String =
    s"ConcreteHiddenNode(neo4jId=$neo4jId, name=$name, valueIndex=$valueIndex, value=$value, ioNode=$ioNode)"

object ConcreteHiddenNode:
  def apply[F[_]: Concurrent](
      neo4jId: Neo4jId,
      name: Name,
      valueIndex: Index,
      ioNode: IoNode[F]
  ): F[ConcreteHiddenNode[F]] =
    for
      value <- ioNode.variable.valueForIndex(valueIndex)
      state <- AtomicCell[F].of[NodeState](InitNodeState)
      node = new ConcreteHiddenNode[F](state, neo4jId, name, valueIndex, value, ioNode)
      _ <- ioNode.addConcreteNode(node)
    yield node
