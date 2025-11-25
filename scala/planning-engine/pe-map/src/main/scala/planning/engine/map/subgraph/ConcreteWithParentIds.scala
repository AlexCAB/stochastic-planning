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
| created: 2025-09-01 |||||||||||*/

package planning.engine.map.subgraph

import cats.MonadThrow
import planning.engine.common.values.node.HnId
import planning.engine.map.hidden.node.ConcreteNode

final case class ConcreteWithParentIds[F[_]: MonadThrow](
    node: ConcreteNode[F],
    linkParentIds: Set[HnId],
    thenParentIds: Set[HnId]
)
