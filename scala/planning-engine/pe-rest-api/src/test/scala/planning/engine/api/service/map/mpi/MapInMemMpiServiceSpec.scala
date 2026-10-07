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

package planning.engine.api.service.map.mpi

import cats.effect.IO
import cats.effect.cps.*
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.api.model.map.extensions.mpi.MapAddSamplesRequestEx.*
import planning.engine.api.model.map.extensions.mpi.MapInitRequestEx.*
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.api.model.map.{MapResetResponse, TestMpiData}
import planning.engine.api.service.map.mpi.MapInMemMpiService
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.MapMpi
import planning.engine.planner.mpi.model.data.samples.Sample

class MapInMemMpiServiceSpec extends UnitSpecWithData with AsyncIdiomaticMockito with TestMpiData:

  private class CaseData extends Case:
    val mapMpiStub: MapMpi[IO] = mock[MapMpi[IO]]
    val service = new MapInMemMpiService(mapMpiStub)

  "MapInMemMpiService.getState" should:
    "return no state for in-mem map" in newCase[CaseData]: (tn, data) =>
      data.service.getState.logValue(tn).asserting(_ mustBe None)

  "MapInMemMpiService.load(...)" should:
    "fail since load is not supported for in-mem map" in newCase[CaseData]: (tn, data) =>
      data.service
        .load(testMapLoadRequest).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Load operation is not supported"))

  "MapInMemMpiService.init(...)" should:
    "initialize in-mem map" in newCase[CaseData]: (tn, data) =>
      val metadata = testMapInitRequest.metadata[IO].unsafeRunSync()
      val inVars = testMapInitRequest.inVars[IO].unsafeRunSync()
      val outVars = testMapInitRequest.outVars[IO].unsafeRunSync()

      data.mapMpiStub.init(metadata, inVars, outVars) returns IO.unit

      async[IO]:
        val response = data.service.init(testMapInitRequest).logValue(tn).await

        data.mapMpiStub.init(metadata, inVars, outVars) was called
        response.mapName mustBe Some(metadata.name)
        response.numInputNodes mustBe inVars.size
        response.numOutputNodes mustBe outVars.size

  "MapInMemMpiService.reset()" should:
    "reset in-mem map" in newCase[CaseData]: (tn, data) =>
      data.mapMpiStub.reset() returns IO.unit

      async[IO]:
        val response = data.service.reset().logValue(tn).await

        data.mapMpiStub.reset() was called
        response mustBe MapResetResponse.emptyInMem

  "MapInMemMpiService.addSamples(...)" should:
    "add new samples to the map" in newCase[CaseData]: (tn, data) =>
      val (mnIds, nodes) = testMapAddSamplesRequest.toNodes[IO](vars).unsafeRunSync()
      val samples = testMapAddSamplesRequest.toSamples[IO](mnIds).unsafeRunSync()
      val addedSamples: Map[SampleId, Sample.Man] = samples.toList.zipWithIndex
        .map((s, i) => SampleId(i + 1L) -> s).toMap

      data.mapMpiStub.getIoVars returns IO.pure(vars)
      data.mapMpiStub.addSamples(samples, nodes) returns IO.pure(addedSamples)

      async[IO]:
        val response = data.service.addSamples(testMapAddSamplesRequest).logValue(tn, "response").await

        data.mapMpiStub.getIoVars was called
        data.mapMpiStub.addSamples(samples, nodes) was called
        response.addedSamples.toSet mustEqual addedSamples.map((id, s) => ShortSampleData(id, Some(s.info.name))).toSet
