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
import planning.engine.common.values.io.{IoIndex, IoValue}
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode

final case class ConcreteDcgNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[HnName],
    ioNode: IoNode[F],
    valueIndex: IoIndex
) extends DcgNode[F]:
  lazy val ioValue: IoValue = IoValue(ioNode.name, valueIndex)

object ConcreteDcgNode:
  def apply[F[_]: MonadThrow](node: ConcreteNode[F]): F[ConcreteDcgNode[F]] = new ConcreteDcgNode[F](
    id = node.id,
    name = node.name,
    ioNode = node.ioNode,
    valueIndex = node.valueIndex
  ).pure
