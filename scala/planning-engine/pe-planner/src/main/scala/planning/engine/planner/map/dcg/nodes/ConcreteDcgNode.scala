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
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.text.Description
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode

final case class ConcreteDcgNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[HnName],
    description: Option[Description],
    ioNode: IoNode[F],
    valueIndex: IoIndex
) extends DcgNode[F]:
  lazy val ioValue: IoValue = IoValue(ioNode.name, valueIndex)

object ConcreteDcgNode:
  def apply[F[_]: MonadThrow](node: ConcreteNode[F]): F[ConcreteDcgNode[F]] = new ConcreteDcgNode[F](
    id = node.id,
    name = node.name,
    description = node.description,
    ioNode = node.ioNode,
    valueIndex = node.valueIndex
  ).pure

  def apply[F[_]: MonadThrow](
      hnId: HnId,
      node: ConcreteNode.New,
      getIoNode: IoName => F[IoNode[F]]
  ): F[ConcreteDcgNode[F]] =
    for
        ioNode <- getIoNode(node.ioNodeName)
    yield new ConcreteDcgNode[F](
      id = hnId,
      name = node.name,
      description = node.description,
      ioNode = ioNode,
      valueIndex = node.valueIndex
    )
