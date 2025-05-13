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
import planning.engine.common.values.node.HnIndex
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.hidden.state.edge.EdgeState

final case class HiddenNodeState[F[_]: MonadThrow](
    parents: Vector[HiddenNode[F]],
    children: Vector[EdgeState[F]],
    nextHnIndex: HnIndex
):
  protected def childrenToString(children: Vector[EdgeState[F]]): String =
    s"${this.getClass.getSimpleName}(children=[${children.mkString(", ")}])"

  protected def parentsToString(parents: Vector[HiddenNode[F]]): String =
    s"${this.getClass.getSimpleName}(parents=[${parents.mkString(", ")}])"

  override def toString: String = s"${parentsToString(parents)} ==> ${childrenToString(children)}"

object HiddenNodeState:
  def init[F[_]: MonadThrow]: HiddenNodeState[F] = HiddenNodeState(Vector.empty, Vector.empty, HnIndex.init)
