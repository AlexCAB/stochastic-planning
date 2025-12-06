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
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode
import planning.engine.planner.map.dcg.nodes.MapNode.{Sources, Targets}

class ConcreteMapNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[Name],
    val ioNode: IoNode[F],
    val valueIndex: IoIndex,
    targets: Targets[F],
    sources: Sources[F]
) extends MapNode[F](id, targets, sources)

object ConcreteMapNode:
  def fromMapNode[F[_]: MonadThrow](mapNode: ConcreteNode[F]): F[ConcreteMapNode[F]] = ???
