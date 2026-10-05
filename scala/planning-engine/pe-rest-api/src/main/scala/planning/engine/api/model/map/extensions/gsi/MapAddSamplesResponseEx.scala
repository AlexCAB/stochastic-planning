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

package planning.engine.api.model.map.extensions.gsi

import planning.engine.api.model.map.MapAddSamplesResponse
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name

object MapAddSamplesResponseEx:
  extension (sampleNames: Map[SampleId, Option[Name]])
    def toMapAddSamplesResponse: MapAddSamplesResponse =
      MapAddSamplesResponse(sampleNames.map((id, name) => ShortSampleData(id, name)).toList)
