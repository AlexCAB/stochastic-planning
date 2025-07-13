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
| created: 2025-07-12 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.IO
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import cats.syntax.all.*

class MapAddSamplesResponseSpec extends UnitSpecWithData:
  private class CaseData extends Case:
    lazy val sampleNames = Map(
      SampleId(1) -> Some(Name("Sample 1")),
      SampleId(2) -> None
    )

    lazy val expectedResponse = MapAddSamplesResponse(
      addedSamples = List(
        ShortSampleData(SampleId(1), Some(Name("Sample 1"))),
        ShortSampleData(SampleId(2), None)
      )
    )

  "MapAddSamplesResponse.fromSampleNames(...)" should:
    "create response from sample names" in newCase[CaseData]: (_, data) =>
      MapAddSamplesResponse.fromSampleNames(data.sampleNames).pure[IO].asserting(_ mustEqual data.expectedResponse)
