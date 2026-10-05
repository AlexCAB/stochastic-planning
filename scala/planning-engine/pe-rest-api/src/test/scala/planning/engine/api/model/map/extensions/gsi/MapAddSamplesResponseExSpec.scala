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

package planning.engine.api.model.map.extensions.gsi

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.api.model.map.MapAddSamplesResponse
import planning.engine.api.model.map.extensions.gsi.MapAddSamplesResponseEx.*
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name

class MapAddSamplesResponseExSpec extends UnitSpecWithData:
  private class CaseData extends Case:
    lazy val sampleNames = Map(
      SampleId(1) -> Name.some("Sample 1"),
      SampleId(2) -> None,
    )

    lazy val expectedResponse = MapAddSamplesResponse(
      addedSamples = List(
        ShortSampleData(SampleId(1), Name.some("Sample 1")),
        ShortSampleData(SampleId(2), None),
      ),
    )

  "MapAddSamplesResponseEx.toMapAddSamplesResponse" should:
    "create response from sample names" in newCase[CaseData]: (_, data) =>
      data.sampleNames.toMapAddSamplesResponse.pure[IO].asserting(_ mustEqual data.expectedResponse)
