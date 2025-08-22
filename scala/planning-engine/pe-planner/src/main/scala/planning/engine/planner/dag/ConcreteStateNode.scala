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
| created: 2025-08-14 |||||||||||*/

package planning.engine.planner.dag

import cats.MonadThrow
import cats.effect.Concurrent
import cats.effect.std.AtomicCell
import planning.engine.common.values.node.{HnId, IoIndex, SnId}
import planning.engine.common.values.text.Name
import planning.engine.map.io.node.IoNode
import planning.engine.planner.dag.StateNode.{Parameters, Structure}
import cats.syntax.all.*

final class ConcreteStateNode[F[_]: MonadThrow](
    override val id: SnId,
    override val hnId: HnId,
    override val name: Option[Name],
    val ioNode: IoNode[F],
    val valueIndex: IoIndex,
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
) extends StateNode[F](structure, parameters):

  override def isBelongsToIo(ioNodeName: Name, ioIndex: IoIndex): Boolean =
    ioNode.name == ioNodeName && valueIndex == ioIndex

  override def toString: String = s"ConcreteStateNode(" +
    s"id = $id, hnId = $hnId, name = ${name.toStr}, ioNodeName = ${ioNode.name}, valueIndex = $valueIndex)"

object ConcreteStateNode:
  def apply[F[_]: Concurrent](
      id: SnId,
      hnId: HnId,
      name: Option[Name],
      ioNode: IoNode[F],
      valueIndex: IoIndex,
      linkParents: Set[StateNode[F]],
      thenParents: Set[StateNode[F]],
      initParameters: Parameters
  ): F[ConcreteStateNode[F]] =
    for
      (structure, parameters) <- StateNode.initState(linkParents, thenParents, initParameters)
      node = new ConcreteStateNode(id, hnId, name, ioNode, valueIndex, structure, parameters)
      _ <- linkParents.toList.traverse(_.addLinkChild(node))
      _ <- thenParents.toList.traverse(_.addThenChild(node))
    yield node
