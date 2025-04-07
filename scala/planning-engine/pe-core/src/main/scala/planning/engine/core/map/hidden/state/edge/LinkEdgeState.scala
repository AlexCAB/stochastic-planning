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
| created: 2025-04-08 |||||||||||*/

package planning.engine.core.map.hidden.state.edge

import cats.MonadThrow
import planning.engine.core.map.hidden.node.HiddenNode
import planning.engine.core.map.hidden.state.SampleState

case class LinkEdgeState[F[_]: MonadThrow](
    target: HiddenNode[F],
    samples: Vector[SampleState[F]]
) extends EdgeState[F]
