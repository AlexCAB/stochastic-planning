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
| created: 2025-04-07 |||||||||||*/

package planning.engine.map.hidden.state.node

import cats.MonadThrow
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.hidden.state.edge.EdgeState

trait NodeState:
  protected def childrenToString[F[_]: MonadThrow](children: Vector[EdgeState[F]]): String =
    s"${this.getClass.getSimpleName}(children=[${children.mkString(", ")}])"

  protected def parentsToString[F[_]: MonadThrow](parents: Vector[HiddenNode[F]]): String =
    s"${this.getClass.getSimpleName}(parents=[${parents.mkString(", ")}])"
