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
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.planner.dag.StateNode.{Parameters, Structure}
import cats.syntax.all.*

final class AbstractStateNode[F[_]: MonadThrow](
    val hnId: HnId,
    val name: Option[Name],
    structure: AtomicCell[F, Structure[F]],
    parameters: AtomicCell[F, Parameters]
) extends StateNode[F](structure, parameters):

  override def toString: String = s"AbstractStateNode(hnId = $hnId, name = ${name.toStr})"

object AbstractStateNode:
  def apply[F[_]: Concurrent](
      hnId: HnId,
      name: Option[Name],
      linkParents: Set[StateNode[F]],
      thenParents: Set[StateNode[F]]
  ): F[AbstractStateNode[F]] =
    for
        (structure, parameters) <- StateNode.initState(linkParents, thenParents)
    yield new AbstractStateNode(hnId, name, structure, parameters)
