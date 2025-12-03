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
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.state.MapNodeState.{Patents, Children}

abstract class MapNode[F[_]: MonadThrow](
    val id: HnId,
    patents: Patents[F],
    children: Children[F]
)

object MapNode
