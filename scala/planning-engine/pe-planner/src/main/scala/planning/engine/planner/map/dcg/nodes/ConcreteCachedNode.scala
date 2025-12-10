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
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode

class ConcreteCachedNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[HnName],
    val ioNode: IoNode[F],
    val ioValue: IoValue
) extends CachedNode[F](id)

object ConcreteCachedNode:
  def apply[F[_]: MonadThrow](node: ConcreteNode[F]): F[ConcreteCachedNode[F]] = new ConcreteCachedNode[F](
    id = node.id,
    name = node.name,
    ioNode = node.ioNode,
    ioValue = node.ioValue
  ).pure
