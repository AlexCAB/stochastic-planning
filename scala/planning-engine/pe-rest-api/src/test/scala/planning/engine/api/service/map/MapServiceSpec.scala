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
import cats.syntax.all.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.api.model.map.*
import cats.effect.cps.*
import cats.effect.std.AtomicCell
import org.scalatest.compatible.Assertion
import planning.engine.api.model.map.payload.ShortSampleData
import planning.engine.common.values.db.DbName
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.map.graph.{MapBuilderLike, MapConfig, MapGraphLake, MapMetadata}
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.common.errors.assertionError

class MapServiceSpec extends UnitSpecWithData with AsyncMockFactory with TestApiData:

  private class CaseData extends Case:
    lazy val testNumOfHiddenNodes = 5L
    lazy val mockBuilder = mock[MapBuilderLike[IO]]
    lazy val emptyService = MapService(testConfig, mockBuilder).use(_.pure).unsafeRunSync()

    def makeMockGraph(inNodes: List[InputNode[IO]], outNodes: List[OutputNode[IO]]): MapGraphLake[IO] =
      val ioNodes = (inNodes ++ outNodes).map(node => node.name -> node).toMap
      val mockedGraph = mock[MapGraphLake[IO]]
      (() => mockedGraph.countHiddenNodes).expects().returns(IO.pure(testNumOfHiddenNodes)).once()
      (() => mockedGraph.metadata).expects().returns(MapMetadata(testMapInitRequest.name, None)).once()
      (() => mockedGraph.ioNodes).expects().returns(ioNodes).twice()
      mockedGraph

    lazy val mockedGraph = mock[MapGraphLake[IO]]
    lazy val service =
      new MapService(testConfig, mockBuilder, AtomicCell[IO].of(Some((mockedGraph, testDbName))).unsafeRunSync())

  "MapService.reset()" should:
    "reset map graph" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val testMapName = Name("testMapName")

        (() => data.mockedGraph.metadata).expects().returns(MapMetadata(Some(testMapName), None)).once()

        data.service.reset().logValue(tn, "resetResponse").await mustEqual
          MapResetResponse(Some(testDbName), Some(testMapName))

        data.service.getState.logValue(tn, "state").await mustBe empty

  "MapService.init(...)" should:
    "initialize map graph when none exists" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val expMetadata: MapMetadata = testMapInitRequest.toMetadata[IO].await
        val expInputNodes: List[InputNode[IO]] = testMapInitRequest.toInputNodes[IO].await
        val expOutputNodes: List[OutputNode[IO]] = testMapInitRequest.toOutputNodes[IO].await
        val mockGraph = data.makeMockGraph(expInputNodes, expOutputNodes)

        data.mockBuilder.init
          .expects(testDbName, testConfig, expMetadata, expInputNodes, expOutputNodes)
          .returns(IO.pure(mockGraph))
          .once()

        val mapInfo: MapInfoResponse = data.emptyService.init(testMapInitRequest).logValue(tn, "mapInfo").await

        mapInfo.mapName mustEqual testMapInitRequest.name
        mapInfo.numInputNodes mustEqual testMapInitRequest.inputNodes.size
        mapInfo.numOutputNodes mustEqual testMapInitRequest.outputNodes.size
        mapInfo.numHiddenNodes mustEqual data.testNumOfHiddenNodes

  "MapService.load(...)" should:
    "load map graph when none exists" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val mockGraph = data.makeMockGraph(List(), List())

        data.mockBuilder.load.expects(testDbName, testConfig).returns(IO.pure(mockGraph)).once()

        val mapInfo = data.emptyService.load(testMapLoadRequest).logValue(tn, "mapInfo").await
        mapInfo.mapName mustEqual testMapInitRequest.name
        mapInfo.numInputNodes mustEqual 0
        mapInfo.numOutputNodes mustEqual 0
        mapInfo.numHiddenNodes mustEqual data.testNumOfHiddenNodes

  "MapService.addSamples(...)" should:
    "add new samples to the map" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val testHnIdMap = Map(
          testConNodeDef1.name -> HnId(101L),
          testConNodeDef2.name -> HnId(102L),
          testAbsNodeDef1.name -> HnId(103L),
          testAbsNodeDef2.name -> HnId(104L)
        )

        val findHnIdsByNamesRes: Map[Name, List[HnId]] = Map(
          testConNodeDef1.name -> List(testHnIdMap(testConNodeDef1.name)),
          testAbsNodeDef1.name -> List(testHnIdMap(testAbsNodeDef1.name))
        )

        val newConcreteNodesRes = Map(testHnIdMap(testConNodeDef2.name) -> Some(testConNodeDef2.name))
        val newAbstractNodesRes = Map(testHnIdMap(testAbsNodeDef2.name) -> Some(testAbsNodeDef2.name))

        val expectedSampleNewList = Sample.ListNew(
          testMapAddSamplesRequest.samples.map: sampleData =>
            Sample.New(
              probabilityCount = sampleData.probabilityCount,
              utility = sampleData.utility,
              name = sampleData.name,
              description = sampleData.description,
              edges = sampleData.edges.map(edge =>
                SampleEdge.New(
                  source = testHnIdMap(edge.sourceHnName),
                  target = testHnIdMap(edge.targetHnName),
                  edgeType = edge.edgeType
                )
              )
            )
        )

        val testResponse = MapAddSamplesResponse(
          testMapAddSamplesRequest.samples.zipWithIndex.map((data, i) => ShortSampleData(SampleId(i + 1), data.name))
        )

        data.mockedGraph.getIoNode
          .expects(*)
          .onCall: name =>
            ioNodes.get(name) match
              case Some(node) => IO.pure(node)
              case None       => "No IoNode found for name: $name".assertionError
          .once()

        data.mockedGraph.findHnIdsByNames
          .expects(testMapAddSamplesRequest.hnNames)
          .returns(IO.pure(findHnIdsByNamesRes))
          .once()

        data.mockedGraph.newConcreteNodes
          .expects(ConcreteNode.ListNew.of(testConNodeNew2))
          .returns(IO.pure(newConcreteNodesRes))
          .once()

        data.mockedGraph.newAbstractNodes
          .expects(AbstractNode.ListNew.of(testAbsNodeDef2.toNew))
          .returns(IO.pure(newAbstractNodesRes))
          .once()

        data.mockedGraph.addNewSamples
          .expects(expectedSampleNewList)
          .returns(IO.pure(testResponse.addedSamples.map(_.id)))
          .once()

        data.mockedGraph.getSampleNames
          .expects(testResponse.addedSamples.map(_.id))
          .returns(IO.pure(testResponse.addedSamples.map(s => s.id -> s.name).toMap))
          .once()

        val gotResponse: MapAddSamplesResponse = data
          .service.addSamples(testMapAddSamplesRequest).logValue(tn, "response").await

        gotResponse mustEqual testResponse
