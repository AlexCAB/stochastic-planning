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
| created: 2025-12-01 |||||||||||*/

package planning.engine.planner.map.dcg.nodes

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.text.Description
import planning.engine.map.hidden.node.AbstractNode

final case class AbstractDcgNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[HnName],
    description: Option[Description]
) extends DcgNode[F]

object AbstractDcgNode:
  def apply[F[_]: MonadThrow](node: AbstractNode[F]): F[AbstractDcgNode[F]] = new AbstractDcgNode[F](
    id = node.id,
    name = node.name,
    description = node.description
  ).pure
  
  def apply[F[_]: MonadThrow](hnId: HnId, node: AbstractNode.New): F[AbstractDcgNode[F]] =
    new AbstractDcgNode[F](
      id = hnId,
      name = node.name,
      description = node.description,
    ).pure
