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
import planning.engine.api.model.map.{MapAddSamplesResponse, MapResetResponse, TestApiData}
import cats.effect.cps.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.planner.map.MapInMemLike

class MapInMemServiceSpec extends UnitSpecWithData with AsyncMockFactory with TestApiData:

  private class CaseData extends Case:
    val mapInMemStub = stub[MapInMemLike[IO]]
    val service = new MapInMemService(mapInMemStub)

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
      async[IO]:
        val metadata = testMapInitRequest.toMetadata[IO].unsafeRunSync()
        val inputNodes = testMapInitRequest.toInputNodes[IO].unsafeRunSync()
        val outputNodes = testMapInitRequest.toOutputNodes[IO].unsafeRunSync()

        data.mapInMemStub.init.when(metadata, inputNodes, outputNodes).returning(IO.unit).once()

        val response = data.service.init(testMapInitRequest).logValue(tn).await

        response.mapName mustBe metadata.name
        response.numInputNodes mustBe inputNodes.size
        response.numOutputNodes mustBe outputNodes.size

  "MapInMemService.reset()" should:
    "reset in-mem map" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        (() => data.mapInMemStub.reset()).when().returning(IO.unit).once()
        data.service.reset().logValue(tn).await mustBe MapResetResponse.emptyInMem[IO].await

  "MapService.addSamples(...)" should :
    "add new samples to the map" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val addedSamples = testResponse.addedSamples
          .map(s => s.id -> testSample.copy(data = testSampleData.copy(id = s.id, name = s.name)))
          .toMap

        data.mapInMemStub.getIoNode
          .when(*)
          .onCall: name =>
            ioNodes.get(name) match
              case Some(node) => IO.pure(node)
              case None => fail(s"No IoNode found for name: $name")
          .once()

        data.mapInMemStub.findHnIdsByNames
          .when(testMapAddSamplesRequest.hnNames.toSet)
          .returns(IO.pure(findHnIdsByNamesRes.map((i, ns) => i -> ns.toSet)))
          .once()

        data.mapInMemStub.addNewConcreteNodes
          .when(ConcreteNode.ListNew.of(testConNodeNew2))
          .returns(IO.pure(newConcreteNodesRes))
          .once()

        data.mapInMemStub.addNewAbstractNodes
          .when(AbstractNode.ListNew.of(testAbsNodeDef2.toNew))
          .returns(IO.pure(newAbstractNodesRes))
          .once()

        data.mapInMemStub.addNewSamples
          .when(expectedSampleNewList)
          .returns(IO.pure(addedSamples))
          .once()

        val gotResponse: MapAddSamplesResponse = data
          .service.addSamples(testMapAddSamplesRequest).logValue(tn, "response").await

        gotResponse mustEqual testResponse
