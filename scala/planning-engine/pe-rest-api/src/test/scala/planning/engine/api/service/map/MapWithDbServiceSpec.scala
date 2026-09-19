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
| created: 2025-04-25 |||||||||||*/

package planning.engine.api.service.map

import cats.effect.IO
import cats.effect.cps.*
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.mockito.scalatest.AsyncIdiomaticMockito
import org.scalatest.compatible.Assertion
import planning.engine.api.model.map.*
import planning.engine.api.service.map.withdb.MapWithDbService
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.db.DbName
import planning.engine.common.values.text.Name
import planning.engine.map.data.MapMetadata
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.{MapBuilderLike, MapGraphLake}
import planning.engine.api.model.map.extensions.gsi.MapInitRequestEx.*

class MapWithDbServiceSpec extends UnitSpecWithData with AsyncIdiomaticMockito with TestApiData:

  private class CaseData extends Case:
    lazy val testNumOfHiddenNodes = 5L
    lazy val mockBuilder: MapBuilderLike[IO] = mock[MapBuilderLike[IO]]
    lazy val emptyService = MapWithDbService(testConfig, mockBuilder).use(_.pure).unsafeRunSync()

    def makeMockGraph(inNodes: List[InputNode[IO]], outNodes: List[OutputNode[IO]]): MapGraphLake[IO] =
      val ioNodes = (inNodes ++ outNodes).map(node => node.name -> node).toMap
      val mockedGraph = mock[MapGraphLake[IO]]
      mockedGraph.countHiddenNodes returns IO.pure(testNumOfHiddenNodes)
      mockedGraph.metadata returns MapMetadata(testMapInitRequest.name, None)
      mockedGraph.ioNodes returns ioNodes
      mockedGraph

    def verifyMockGraph(mockedGraph: MapGraphLake[IO]): Unit =
      mockedGraph.countHiddenNodes was called
      mockedGraph.metadata was called
      mockedGraph.ioNodes wasCalled twice

    lazy val mockedGraph: MapGraphLake[IO] = mock[MapGraphLake[IO]]
    lazy val service =
      new MapWithDbService(testConfig, mockBuilder, AtomicCell[IO].of(Some((mockedGraph, testDbName))).unsafeRunSync())

  "MapService.reset(...)" should:
    "reset map graph" in newCase[CaseData]: (tn, data) =>
      val testMapName = Name("testMapName")

      data.mockedGraph.metadata returns MapMetadata(Some(testMapName), None)

      async[IO]:
        data.service.reset().logValue(tn, "resetResponse").await mustEqual
          MapResetResponse(Some(testDbName), Some(testMapName))

        data.mockedGraph.metadata was called
        data.service.getState.logValue(tn, "state").await mustBe empty

  "MapService.init(...)" should:
    "initialize map graph when none exists" in newCase[CaseData]: (tn, data) =>
      val expMetadata: MapMetadata = testMapInitRequest.toMetadata[IO].unsafeRunSync()
      val expInputNodes: List[InputNode[IO]] = testMapInitRequest.toInputNodes[IO].unsafeRunSync()
      val expOutputNodes: List[OutputNode[IO]] = testMapInitRequest.toOutputNodes[IO].unsafeRunSync()
      val mockGraph = data.makeMockGraph(expInputNodes, expOutputNodes)

      data.mockBuilder.init(testDbName, testConfig, expMetadata, expInputNodes, expOutputNodes) returns
        IO.pure(mockGraph)

      async[IO]:
        val mapInfo: MapInfoResponse = data.emptyService.init(testMapInitRequest).logValue(tn, "mapInfo").await

        data.mockBuilder.init(testDbName, testConfig, expMetadata, expInputNodes, expOutputNodes) was called
        data.verifyMockGraph(mockGraph)
        mapInfo.mapName mustEqual testMapInitRequest.name
        mapInfo.numInputNodes mustEqual testMapInitRequest.inputNodes.size
        mapInfo.numOutputNodes mustEqual testMapInitRequest.outputNodes.size
        mapInfo.numHiddenNodes mustEqual data.testNumOfHiddenNodes

  "MapService.load(...)" should:
    "load map graph when none exists" in newCase[CaseData]: (tn, data) =>
      val mockGraph = data.makeMockGraph(List(), List())

      data.mockBuilder.load(testDbName, testConfig) returns IO.pure(mockGraph)

      async[IO]:
        val mapInfo = data.emptyService.load(testMapLoadRequest).logValue(tn, "mapInfo").await

        data.mockBuilder.load(testDbName, testConfig) was called
        data.verifyMockGraph(mockGraph)
        mapInfo.mapName mustEqual testMapInitRequest.name
        mapInfo.numInputNodes mustEqual 0
        mapInfo.numOutputNodes mustEqual 0
        mapInfo.numHiddenNodes mustEqual data.testNumOfHiddenNodes

// TODO To refactor when DB added:

//  "MapService.addSamples(...)" should:
//    "add new samples to the map" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        data.mockedGraph.getIoNode
//          .expects(*)
//          .onCall: name =>
//            ioNodes.get(name) match
//              case Some(node) => IO.pure(node)
//              case None       => s"No IoNode found for name: $name".assertionError
//          .once()
//
//        data.mockedGraph.findHnIdsByNames
//          .expects(testMapAddSamplesRequest.hnNames)
//          .returns(IO.pure(findHnIdsByNamesRes))
//          .once()
//
//        data.mockedGraph.newConcreteNodes
//          .expects(ConcreteNode.ListNew.of(testConNodeNew2))
//          .returns(IO.pure(newConcreteNodesRes))
//          .once()
//
//        data.mockedGraph.newAbstractNodes
//          .expects(AbstractNode.ListNew.of(testAbsNodeDef2.toNew))
//          .returns(IO.pure(newAbstractNodesRes))
//          .once()
//
//        data.mockedGraph.addNewSamples
//          .expects(expectedSampleNewList)
//          .returns(IO.pure(testResponse.addedSamples.map(s => testSample.copy(data = testSampleData.copy(id = s.id)))))
//          .once()
//
//        data.mockedGraph.getSampleNames
//          .expects(testResponse.addedSamples.map(_.id))
//          .returns(IO.pure(testResponse.addedSamples.map(s => s.id -> s.name).toMap))
//          .once()
//
//        val gotResponse: MapAddSamplesResponse = data
//          .service.addSamples(testMapAddSamplesRequest).logValue(tn, "response").await
//
//        gotResponse mustEqual testResponse
