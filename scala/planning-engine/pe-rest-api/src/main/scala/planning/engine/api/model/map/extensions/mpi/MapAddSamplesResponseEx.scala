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

import planning.engine.api.model.map.MapAddSamplesResponse
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.model.data.samples.Sample

object MapAddSamplesResponseEx:
  extension (ids: Map[SampleId, Sample.Man])
    def toMapAddSamplesResponse: MapAddSamplesResponse = MapAddSamplesResponse(
      ids.toList.map((id, data) => ShortSampleData(id, Some(data.info.name))),
    )
