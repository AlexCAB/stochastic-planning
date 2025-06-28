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

package planning.engine.map.hidden.edge

import cats.MonadThrow
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.SampleEdge

case class ThenEdge[F[_]: MonadThrow](
    source: HiddenNode[F],
    target: HiddenNode[F],
    samples: List[SampleEdge]
) extends HiddenEdge[F]
