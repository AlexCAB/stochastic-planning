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
| created: 2025-04-08 |||||||||||*/

package planning.engine.core.map.hidden.state.node

import cats.MonadThrow
import planning.engine.core.map.hidden.node.HiddenNode
import planning.engine.core.map.hidden.state.edge.EdgeState

case class IntermediateNodeState[F[_]: MonadThrow](
    parents: Vector[HiddenNode[F]],
    children: Vector[EdgeState[F]]
) extends NodeState:
  override def toString: String = s"${parentsToString(parents)} ==> ${childrenToString(children)}"
