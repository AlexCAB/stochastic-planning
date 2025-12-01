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
import StateNode.{Parameters, Structure}
import cats.syntax.all.*

final class AbstractStateNode[F[_]: MonadThrow](
    override val id: SnId,
    override val hnId: HnId,
    override val name: Option[Name],
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
) extends StateNode[F](structure, parameters):
  
  override val isConcrete: Boolean = false
  override def toString: String = s"AbstractStateNode(id = $id, hnId = $hnId, name = ${name.toStr})"

object AbstractStateNode:
  def apply[F[_]: Concurrent](
      id: SnId,
      hnId: HnId,
      name: Option[Name],
      linkParents: Set[StateNode[F]],
      thenParents: Set[StateNode[F]],
      initParameters: Parameters
  ): F[AbstractStateNode[F]] =
    for
      (structure, parameters) <- StateNode.initState(initParameters)
      node = new AbstractStateNode(id, hnId, name, structure, parameters)
      _ <- linkParents.toList.traverse(_.joinNextLink(node))
      _ <- thenParents.toList.traverse(_.joinNextThen(node))
    yield node
