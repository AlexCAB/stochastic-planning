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
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode
import planning.engine.planner.map.dcg.state.MapNodeState.{Children, Patents}

class ConcreteMapNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[Name],
    val ioNode: IoNode[F],
    val valueIndex: IoIndex,
    patents: Patents[F],
    children: Children[F]
) extends MapNode[F](id, patents, children)

object ConcreteMapNode:
  def fromMapNode[F[_]: MonadThrow](mapNode: ConcreteNode[F]): F[ConcreteMapNode[F]] = ???
