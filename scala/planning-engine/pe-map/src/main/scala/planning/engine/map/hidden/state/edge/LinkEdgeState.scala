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

package planning.engine.map.hidden.state.edge

import cats.MonadThrow
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.SampleEdgeState

case class LinkEdgeState[F[_]: MonadThrow](
    target: HiddenNode[F],
    samples: List[SampleEdgeState]
) extends EdgeState[F]
