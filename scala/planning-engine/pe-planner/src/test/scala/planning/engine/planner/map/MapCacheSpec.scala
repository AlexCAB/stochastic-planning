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
| created: 2025-12-14 |||||||||||*/

package planning.engine.planner.map

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.validation.ValidationError
import planning.engine.common.values.io.{IoIndex, IoValue}
import planning.engine.common.values.node.HnId
import planning.engine.map.subgraph.MapSubGraph
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.map.MapGraphLake
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.planner.map.dcg.state.DcgState

class MapCacheSpec extends UnitSpecWithData with AsyncMockFactory with MapTestData:

  private class CaseData extends Case with SimpleMemStateTestData:
    private val mapGraphMock = stub[MapGraphLake[IO]]
    val mapCache = MapCache[IO](mapGraphMock).unsafeRunSync()

    def setMapGraphMock(
        expectedValues: List[IoValue],
        expectedLoadedSamples: List[SampleId],
        result: MapSubGraph[IO]
    ): Unit = mapGraphMock.loadSubgraphForIoValue
      .when(*, *)
      .onCall: (values, loadedSamples) =>
        for
          _ <- IO.delay(values mustBe expectedValues)
          _ <- IO.delay(loadedSamples mustBe expectedLoadedSamples)
        yield result
      .once()

  "MapCache.load(...)" should:
    "load map graph from cache" in newCase[CaseData]: (n, data) =>
      data.setMapGraphMock(data.ioValues, List(data.sampleId1), data.mapSubGraph)

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(n)
        .asserting(_ mustBe data.mapSubGraph)

    "fail sub graph is failed" in newCase[CaseData]: (n, data) =>
      data.setMapGraphMock(data.ioValues, List(data.sampleId1), data.mapSubGraph.copy(concreteNodes = List()))

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(n)
        .assertThrowsError[ValidationError](_.getMessage must startWith("Validation failed for MapSubGraph"))

    "fail if superfluous nodes presented" in newCase[CaseData]: (n, data) =>
      val invConNodes = data.mapSubGraph.concreteNodes :+ makeConcreteNode(HnId(-1), IoIndex(-2))
      data.setMapGraphMock(data.ioValues, List(data.sampleId1), data.mapSubGraph.copy(concreteNodes = invConNodes))

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(n)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Superfluous nodes presented"))

    "fail if abstract nodes none empty" in newCase[CaseData]: (n, data) =>
      val invNode = List(makeAbstractNode(HnId(-3)))
      data.setMapGraphMock(data.ioValues, List(data.sampleId1), data.mapSubGraph.copy(abstractNodes = invNode))

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(n)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Abstract nodes should not be loaded"))

  "MapCache.getForIoValues(...)" should:
    val notInMap = IoValue(testIntInNode.name, IoIndex(-1))

    "get nodes from map graph and update empty cache" in newCase[CaseData]: (n, data) =>
      val request = data.ioValues :+ notInMap
      data.setMapGraphMock(request, List(), data.mapSubGraph)

      async[IO]:
        val (loaded, notFound) = data.mapCache.getForIoValues(request.toSet).logValue(n).await
        loaded mustBe data.dcgNodes
        notFound mustBe Set(notInMap)

        val state = data.mapCache.getMapState.await
        state.ioValues mustBe data.dcgState.ioValues
        state.concreteNodes mustBe data.dcgState.concreteNodes
        state.abstractNodes mustBe empty
        state.edges mustBe data.dcgState.edges
        state.forwardLinks mustBe data.dcgState.forwardLinks
        state.backwardLinks mustBe data.dcgState.backwardLinks
        state.forwardThen mustBe data.dcgState.forwardThen
        state.backwardThen mustBe data.dcgState.backwardThen
        state.samplesData mustBe data.dcgState.samplesData

    "get nodes from cache" in newCase[CaseData]: (n, data) =>
      data.setMapGraphMock(List(notInMap), data.dcgState.samplesData.keys.toList, MapSubGraph.emptySubGraph[IO])

      async[IO]:
        data.mapCache.setMapState(data.dcgState).await
        val (loaded, notFound) = data.mapCache.getForIoValues(data.ioValues.toSet + notInMap).logValue(n).await

        loaded mustBe data.dcgNodes
        notFound mustBe Set(notInMap)
        data.mapCache.getMapState.await mustBe data.dcgState // state should be updated if nothing loaded from map graph
