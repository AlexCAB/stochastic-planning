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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.mpi

import cats.MonadThrow
import planning.engine.api.model.map.MapAddSamplesResponse
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.model.data.samples.Sample

object MapAddSamplesResponseEx:
  extension (ids: Map[SampleId, Sample.Man])
    def toMapAddSamplesResponse[F[_]: MonadThrow]: F[MapAddSamplesResponse] = ???
