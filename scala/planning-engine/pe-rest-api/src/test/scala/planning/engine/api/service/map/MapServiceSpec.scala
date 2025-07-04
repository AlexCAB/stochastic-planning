///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 2025-04-25 |||||||||||*/
//
//package planning.engine.api.service.map
//
//import cats.effect.IO
//import org.scalamock.scalatest.AsyncMockFactory
//import planning.engine.common.UnitSpecIO
//import planning.engine.api.model.map.*
//import cats.effect.cps.*
//import org.scalatest.compatible.Assertion
//import planning.engine.map.graph.{MapBuilderLike, MapGraphLake, MapMetadata}
//import planning.engine.map.io.node.{InputNode, OutputNode}
//
//class MapServiceSpec extends UnitSpecIO with AsyncMockFactory:
//  private val testMapInitRequest = MapInitRequest(
//    name = Some("testMapName"),
//    description = None,
//    inputNodes = List(BooleanIoNode("boolDef", Set(true, false))),
//    outputNodes = List(IntIoNode("intDef", min = 0, max = 10))
//  )
//
//  private val testNumOfHiddenNodes = 5L
//
//  private def makeMockGraph(inNodes: List[InputNode[IO]], outNodes: List[OutputNode[IO]]): MapGraphLake[IO] =
//    val mockGraph = mock[MapGraphLake[IO]]
//    (() => mockGraph.countHiddenNodes).expects().returns(IO.pure(testNumOfHiddenNodes)).once()
//    (() => mockGraph.metadata).expects().returns(MapMetadata.withName(testMapInitRequest.name)).once()
//    (() => mockGraph.inputNodes).expects().returns(inNodes).once()
//    (() => mockGraph.outputNodes).expects().returns(outNodes).once()
//    mockGraph
//
//  private def newService(test: (MapBuilderLike[IO], MapService[IO]) => IO[Assertion]): IO[Assertion] =
//    val mockBuilder = mock[MapBuilderLike[IO]]
//    MapService(mockBuilder).use(service => test(mockBuilder, service))
//
//  "MapService.init()" should:
//    "initialize knowledge graph when none exists" in newService: (mockBuilder, service) =>
//      async[IO]:
//        val expMetadata = testMapInitRequest.toMetadata[IO].await
//        val expInputNodes = testMapInitRequest.toInputNodes[IO].await
//        val expOutputNodes = testMapInitRequest.toOutputNodes[IO].await
//        val mockGraph = makeMockGraph(expInputNodes, expOutputNodes)
//
//        mockBuilder.init
//          .expects(expMetadata, expInputNodes, expOutputNodes)
//          .returns(IO.pure(mockGraph))
//          .once()
//
//        val mapInfo = service.init(testMapInitRequest).await
//
//        mapInfo.mapName mustEqual testMapInitRequest.name
//        mapInfo.numInputNodes mustEqual testMapInitRequest.inputNodes.size
//        mapInfo.numOutputNodes mustEqual testMapInitRequest.outputNodes.size
//        mapInfo.numHiddenNodes mustEqual testNumOfHiddenNodes
//
//  "MapService.load()" should:
//    "load knowledge graph when none exists" in newService: (mockBuilder, service) =>
//      async[IO]:
//        val mockGraph = makeMockGraph(List(), List())
//        (() => mockBuilder.load).expects().returns(IO.pure(mockGraph)).once()
//        val mapInfo = service.load.await
//
//        mapInfo.mapName mustEqual testMapInitRequest.name
//        mapInfo.numInputNodes mustEqual 0
//        mapInfo.numOutputNodes mustEqual 0
//        mapInfo.numHiddenNodes mustEqual testNumOfHiddenNodes
