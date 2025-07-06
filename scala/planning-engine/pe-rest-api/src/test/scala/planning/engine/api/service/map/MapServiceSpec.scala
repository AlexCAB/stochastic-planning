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
import org.scalatest.compatible.Assertion
import planning.engine.common.values.text.Name
import planning.engine.map.graph.{MapBuilderLike, MapConfig, MapGraphLake, MapMetadata}
import planning.engine.map.io.node.{InputNode, OutputNode}

class MapServiceSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case:
    val testNumOfHiddenNodes = 5L

    val testMapInitRequest = MapInitRequest(
      name = Some("testMapName"),
      description = None,
      inputNodes = List(BooleanIoNode("boolDef", Set(true, false))),
      outputNodes = List(IntIoNode("intDef", min = 0, max = 10))
    )

    val testConfig: MapConfig = MapConfig(
      initNextHnId = 100L,
      initNextSampleId = 200L,
      initSampleCount = 300L,
      initNextHnIndex = 400L,
      samplesName = "samples"
    )

    val mockBuilder = mock[MapBuilderLike[IO]]
    val service = MapService(testConfig, mockBuilder).use(_.pure).unsafeRunSync()

    def makeMockGraph(inNodes: List[InputNode[IO]], outNodes: List[OutputNode[IO]]): MapGraphLake[IO] =
      val mockGraph = mock[MapGraphLake[IO]]
      (() => mockGraph.countHiddenNodes).expects().returns(IO.pure(testNumOfHiddenNodes)).once()
      (() => mockGraph.metadata).expects().returns(MapMetadata(testMapInitRequest.name.map(Name.apply), None)).once()
      (() => mockGraph.inputNodes).expects().returns(inNodes).once()
      (() => mockGraph.outputNodes).expects().returns(outNodes).once()
      mockGraph

  "MapService.init()" should:
    "initialize knowledge graph when none exists" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val expMetadata = data.testMapInitRequest.toMetadata[IO].await
        val expInputNodes = data.testMapInitRequest.toInputNodes[IO].await
        val expOutputNodes = data.testMapInitRequest.toOutputNodes[IO].await
        val mockGraph = data.makeMockGraph(expInputNodes, expOutputNodes)

        data.mockBuilder.init
          .expects(data.testConfig, expMetadata, expInputNodes, expOutputNodes)
          .returns(IO.pure(mockGraph))
          .once()

        val mapInfo = data.service.init(data.testMapInitRequest).await

        mapInfo.mapName mustEqual data.testMapInitRequest.name
        mapInfo.numInputNodes mustEqual data.testMapInitRequest.inputNodes.size
        mapInfo.numOutputNodes mustEqual data.testMapInitRequest.outputNodes.size
        mapInfo.numHiddenNodes mustEqual data.testNumOfHiddenNodes

  "MapService.load()" should:
    "load knowledge graph when none exists" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val mockGraph = data.makeMockGraph(List(), List())
        data.mockBuilder.load.expects(data.testConfig).returns(IO.pure(mockGraph)).once()

        val mapInfo = data.service.load.await
        mapInfo.mapName mustEqual data.testMapInitRequest.name
        mapInfo.numInputNodes mustEqual 0
        mapInfo.numOutputNodes mustEqual 0
        mapInfo.numHiddenNodes mustEqual data.testNumOfHiddenNodes
