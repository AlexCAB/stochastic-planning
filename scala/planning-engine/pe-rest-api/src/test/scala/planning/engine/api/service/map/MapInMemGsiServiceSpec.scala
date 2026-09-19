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
| created: 2025-12-25 |||||||||||*/

package planning.engine.api.service.map

import cats.effect.IO
import cats.effect.cps.*
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.api.model.map.{MapAddSamplesResponse, MapResetResponse, TestApiData}
import planning.engine.api.service.map.inmem.MapInMemGsiService
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.planner.gsi.map.MapInMemGsiLike
import planning.engine.planner.gsi.map.dcg.samples.DcgSample
import planning.engine.api.model.map.extensions.gsi.MapInitRequestEx.*

class MapInMemGsiServiceSpec extends UnitSpecWithData with AsyncIdiomaticMockito with TestApiData:

  private class CaseData extends Case:
    val mapInMemStub: MapInMemGsiLike[IO] = mock[MapInMemGsiLike[IO]]
    val service = new MapInMemGsiService(mapInMemStub)

  "MapInMemService.getState" should:
    "get map state from in-mem map" in newCase[CaseData]: (tn, data) =>
      data.service.getState.logValue(tn).asserting(_ mustBe None)

  "MapInMemService.load(...)" should:
    "load map into in-mem map" in newCase[CaseData]: (tn, data) =>
      data.service
        .load(testMapLoadRequest).logValue(tn).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Load operation is not supported"))

  "MapInMemService.init(...)" should:
    "initialize in-mem map" in newCase[CaseData]: (tn, data) =>
      val metadata = testMapInitRequest.toMetadata[IO].unsafeRunSync()
      val inputNodes = testMapInitRequest.toInputNodes[IO].unsafeRunSync()
      val outputNodes = testMapInitRequest.toOutputNodes[IO].unsafeRunSync()

      data.mapInMemStub.init(metadata, inputNodes, outputNodes) returns IO.unit

      async[IO]:
        val response = data.service.init(testMapInitRequest).logValue(tn).await

        data.mapInMemStub.init(metadata, inputNodes, outputNodes) was called
        response.mapName mustBe metadata.name
        response.numInputNodes mustBe inputNodes.size
        response.numOutputNodes mustBe outputNodes.size

  "MapInMemService.reset()" should:
    "reset in-mem map" in newCase[CaseData]: (tn, data) =>
      data.mapInMemStub.reset() returns IO.unit

      async[IO]:
        val response = data.service.reset().logValue(tn).await

        data.mapInMemStub.reset() was called
        response mustBe MapResetResponse.emptyInMem[IO].await

  "MapService.addSamples(...)" should:
    "add new samples to the map" in newCase[CaseData]: (tn, data) =>
      val addedSamples: Map[SampleId, DcgSample[IO]] = testResponse.addedSamples
        .map(s => s.id -> testDcgSample.copy(data = testSampleData.copy(id = s.id, name = s.name)))
        .toMap

      data.mapInMemStub.getIoNode(testConNodeDef2.ioNodeName) returns IO.pure(ioNodes(testConNodeDef2.ioNodeName))

      data.mapInMemStub.findHnIdsByNames(testMapAddSamplesRequest.hnNames.toSet) returns
        IO.pure(findHnIdsByNamesRes.map((i, ns) => i -> ns.toSet))

      data.mapInMemStub.addNewConcreteNodes(ConcreteNode.ListNew.of(testConNodeNew2)) returns
        IO.pure(newConcreteNodesRes)

      data.mapInMemStub.addNewAbstractNodes(AbstractNode.ListNew.of(testAbsNodeDef2.toNew)) returns
        IO.pure(newAbstractNodesRes)

      data.mapInMemStub.addNewSamples(expectedSampleNewList) returns IO.pure(addedSamples)

      async[IO]:
        val gotResponse: MapAddSamplesResponse = data
          .service.addSamples(testMapAddSamplesRequest).logValue(tn, "response").await

        data.mapInMemStub.getIoNode(testConNodeDef2.ioNodeName) was called
        data.mapInMemStub.findHnIdsByNames(testMapAddSamplesRequest.hnNames.toSet) was called
        data.mapInMemStub.addNewConcreteNodes(ConcreteNode.ListNew.of(testConNodeNew2)) was called
        data.mapInMemStub.addNewAbstractNodes(AbstractNode.ListNew.of(testAbsNodeDef2.toNew)) was called
        data.mapInMemStub.addNewSamples(expectedSampleNewList) was called
        gotResponse mustEqual testResponse
