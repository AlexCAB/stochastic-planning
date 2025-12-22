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
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.HnId
import planning.engine.map.subgraph.MapSubGraph
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.map.MapGraphLake
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.planner.map.dcg.state.DcgState
import planning.engine.planner.map.test.data.SimpleMemStateTestData

class MapCacheSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with SimpleMemStateTestData:
    private val mapGraphMock = stub[MapGraphLake[IO]]
    val mapCache = MapCache[IO](mapGraphMock).unsafeRunSync()

    def setLoadSubgraphForIoValue(
        expectedValues: List[IoValue],
        expectedLoadedSamples: List[SampleId],
        result: MapSubGraph[IO]
    ): Unit = mapGraphMock.loadSubgraphForIoValue.when(*, *)
      .onCall: (values, loadedSamples) =>
        for
          _ <- IO.delay(values mustBe expectedValues)
          _ <- IO.delay(loadedSamples mustBe expectedLoadedSamples)
        yield result
      .once()

    def setAddNewSamples(
        expectedSamples: Sample.ListNew,
        result: List[Sample]
    ): Unit = mapGraphMock.addNewSamples.when(*)
      .onCall: params =>
        for
            _ <- IO.delay(params mustBe expectedSamples)
        yield result
      .once()

  "MapCache.load(...)" should:
    "load map graph from cache" in newCase[CaseData]: (tn, data) =>
      data.setLoadSubgraphForIoValue(data.ioValues, List(data.sampleId1), data.mapSubGraph)

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(tn)
        .asserting(_ mustBe data.mapSubGraph)

    "fail sub graph is failed" in newCase[CaseData]: (tn, data) =>
      data.setLoadSubgraphForIoValue(data.ioValues, List(data.sampleId1), data.mapSubGraph.copy(concreteNodes = List()))

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(tn)
        .assertThrowsError[ValidationError](_.getMessage must startWith("Validation failed for MapSubGraph"))

    "fail if superfluous nodes presented" in newCase[CaseData]: (tn, data) =>
      val invConNodes = data.mapSubGraph.concreteNodes :+ data.makeConcreteNode(HnId(-1))
      data.setLoadSubgraphForIoValue(
        data.ioValues,
        List(data.sampleId1),
        data.mapSubGraph.copy(concreteNodes = invConNodes)
      )

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Superfluous nodes presented"))

    "fail if abstract nodes none empty" in newCase[CaseData]: (tn, data) =>
      val invNode = List(data.makeAbstractNode(HnId(-3)))
      data.setLoadSubgraphForIoValue(
        data.ioValues,
        List(data.sampleId1),
        data.mapSubGraph.copy(abstractNodes = invNode)
      )

      data.mapCache
        .load(data.ioValues.toSet, Set(data.sampleId1))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Abstract nodes should not be loaded"))

  "MapCache.getForIoValues(...)" should:
    "get nodes from map graph and update empty cache" in newCase[CaseData]: (tn, data) =>
      val request = data.ioValues :+ data.testNotInMap
      data.setLoadSubgraphForIoValue(request, List(), data.mapSubGraph)

      async[IO]:
        val (loaded, notFound) = data.mapCache.getForIoValues(request.toSet).logValue(tn).await
        loaded mustBe data.conDcgNodesMap
        notFound mustBe Set(data.testNotInMap)

        val state = data.mapCache.getMapState.await
        state.ioValues mustBe data.dcgStateFromSubGraph.ioValues
        state.concreteNodes mustBe data.dcgStateFromSubGraph.concreteNodes
        state.abstractNodes mustBe empty
        state.edges mustBe data.dcgStateFromSubGraph.edges
        state.forwardLinks mustBe data.dcgStateFromSubGraph.forwardLinks
        state.backwardLinks mustBe data.dcgStateFromSubGraph.backwardLinks
        state.forwardThen mustBe data.dcgStateFromSubGraph.forwardThen
        state.backwardThen mustBe data.dcgStateFromSubGraph.backwardThen
        state.samplesData mustBe data.dcgStateFromSubGraph.samplesData

    "get nodes from cache" in newCase[CaseData]: (tn, data) =>
      data.setLoadSubgraphForIoValue(
        List(data.testNotInMap),
        data.dcgStateFromSubGraph.samplesData.keys.toList,
        MapSubGraph.emptySubGraph[IO]
      )

      async[IO]:
        data.mapCache.setMapState(data.dcgStateFromSubGraph).await

        val (loaded, notFound) = data.mapCache
          .getForIoValues(data.ioValues.toSet + data.testNotInMap).logValue(tn).await

        loaded mustBe data.conDcgNodesMap
        notFound mustBe Set(data.testNotInMap)
        data.mapCache.getMapState.await mustBe data.dcgStateFromSubGraph // state should be updated if nothing loaded from map graph

  "MapCache.addNewSamples(...)" should:
    "add new samples to map graph and update cache" in newCase[CaseData]: (tn, data) =>
      data.setAddNewSamples(data.sampleListNew, data.newSamples)

      async[IO]:
        data.mapCache.setMapState(data.initialDcgState).await

        val result = data.mapCache.addNewSamples(data.sampleListNew).logValue(tn).await
        val state = data.mapCache.getMapState.logValue(tn).await

        result mustBe data.newSamples.map(s => s.data.id -> s).toMap
        state.samplesData mustBe data.newSamples.map(s => s.data.id -> s.data).toMap
