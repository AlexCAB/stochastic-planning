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
import planning.engine.common.errors.assertionError
import cats.syntax.all.*
import neotypes.model.types.Value
import planning.engine.common.properties.*

final case class HiddenNodeState[F[_]: MonadThrow](
    parents: List[HiddenNode[F]],
    children: List[EdgeState[F]],
    nextHnIndex: HnIndex,
    numberOfUsages: Long // Used to cound the number of usages of this node, when decrease to 0, the node is removed
):
  private def childrenToString(children: List[EdgeState[F]]): String =
    s"${this.getClass.getSimpleName}(children=[${children.mkString(", ")}])"

  private def parentsToString(parents: List[HiddenNode[F]]): String =
    s"${this.getClass.getSimpleName}(parents=[${parents.mkString(", ")}])"

  override def toString: String = s"${parentsToString(parents)} ==> ${childrenToString(children)}"

  private[map] def increaseNumUsages: F[HiddenNodeState[F]] = this.copy(numberOfUsages = numberOfUsages + 1L).pure

  private[map] def decreaseNumUsages: F[(HiddenNodeState[F], Boolean)] = numberOfUsages - 1L match
    case 0          => (this.copy(numberOfUsages = 0), true).pure
    case n if n < 0 => s"Seems bug: hidden node with state $this release mode times then allocated".assertionError
    case n          => (this.copy(numberOfUsages = n), false).pure

object HiddenNodeState:
  private[map] def init[F[_]: MonadThrow]: HiddenNodeState[F] = HiddenNodeState(
    List.empty,
    List.empty,
    HnIndex.init,
    numberOfUsages = 1L
  )

  private[map] def fromProperties[F[_]: MonadThrow](properties: Map[String, Value]): F[HiddenNodeState[F]] =
    for
        nextHnIndex <- properties.getValue[F, Long](PROP_NAME.NEXT_HN_INEX).map(HnIndex.apply)
    yield HiddenNodeState(List.empty, List.empty, nextHnIndex, numberOfUsages = 1L)
