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
import planning.engine.common.values.Index
import planning.engine.core.map.sample.SampleData

case class SampleState[F[_]: MonadThrow](
    sourceValue: Index,
    targetValue: Index,
    sample: SampleData[F]
):
  override def toString: String = s"SampleState($sourceValue -- ${sample.id} --> $targetValue)"
