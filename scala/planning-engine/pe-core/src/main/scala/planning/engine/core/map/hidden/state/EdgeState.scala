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
| created: 2025-04-07 |||||||||||*/



package planning.engine.core.map.hidden.state

import cats.MonadThrow
import planning.engine.core.map.hidden.node.HiddenNode

sealed trait EdgeState[F[_]: MonadThrow]:
  val target: HiddenNode[F]
  val samples: Vector[SampleState[F]]
