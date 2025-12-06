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
import planning.engine.common.values.text.Name
import planning.engine.planner.map.dcg.nodes.MapNode.{Sources, Targets}

class AbstractMapNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[Name],
    targets: Targets[F],
    sources: Sources[F]
) extends MapNode[F](id, targets, sources)
