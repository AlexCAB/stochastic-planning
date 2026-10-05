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
| created: 2026-10-06 |||||||||||*/

package planning.engine.api.model.map.extensions.mpi

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.api.model.map.TestMpiData
import planning.engine.api.model.map.extensions.mpi.MapAddSamplesResponseEx.*
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.model.data.samples.Sample

class MapAddSamplesResponseExSpec extends UnitSpecWithData:

  private class CaseData extends Case with TestMpiData:
    lazy val sampleMan2 = sampleMan.copy(info = sampleMan.info.copy(name = Name("sample2")))

    lazy val samples: Map[SampleId, Sample.Man] = Map(
      SampleId(1L) -> sampleMan,
      SampleId(2L) -> sampleMan2,
    )

  "MapAddSamplesResponseEx.toMapAddSamplesResponse" should:
    "create response with sample IDs and names" in newCase[CaseData]: (_, data) =>
      data.samples.toMapAddSamplesResponse.pure[IO].asserting(_.addedSamples.toSet mustEqual Set(
        ShortSampleData(SampleId(1L), Some(data.sampleMan.info.name)),
        ShortSampleData(SampleId(2L), Name.some("sample2")),
      ))

    "create empty response for no samples" in newCase[CaseData]: (_, _) =>
      Map.empty[SampleId, Sample.Man].toMapAddSamplesResponse.pure[IO].asserting(_.addedSamples mustBe empty)
