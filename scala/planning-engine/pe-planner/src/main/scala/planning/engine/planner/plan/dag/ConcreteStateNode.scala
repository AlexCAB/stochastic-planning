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

package planning.engine.planner.plan.dag

import cats.MonadThrow
import cats.effect.Concurrent
import cats.effect.std.AtomicCell
import planning.engine.common.values.node.{HnId, SnId}
import planning.engine.common.values.text.Name
import planning.engine.map.io.node.IoNode
import StateNode.{Parameters, Structure}
import cats.syntax.all.*
import planning.engine.common.values.io.IoIndex

final class ConcreteStateNode[F[_]: MonadThrow](
    override val id: SnId,
    override val hnId: HnId,
    override val name: Option[Name],
    val ioNode: IoNode[F],
    val valueIndex: IoIndex,
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
) extends StateNode[F](structure, parameters):

  def isBelongsToIo(ioNodeName: Name, ioIndex: IoIndex): Boolean =
    ioNode.name == ioNodeName && valueIndex == ioIndex

  override val isConcrete: Boolean = true
  
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
      (structure, parameters) <- StateNode.initState(initParameters)
      node = new ConcreteStateNode(id, hnId, name, ioNode, valueIndex, structure, parameters)
      _ <- linkParents.toList.traverse(_.joinNextLink(node))
      _ <- thenParents.toList.traverse(_.joinNextThen(node))
    yield node
