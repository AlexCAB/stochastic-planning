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
import planning.engine.common.validation.ValidationError
import planning.engine.common.values.io.{IoIndex, IoValue}
import planning.engine.common.values.node.HnId
import planning.engine.map.subgraph.MapSubGraph
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.map.MapGraphLake

class MapCacheSpec extends UnitSpecWithData with AsyncMockFactory with MapTestData:

  private class CaseData extends Case:
    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val sampleId1 = SampleId(1001)
    lazy val loadedSamples = List(SampleId(1001))

    lazy val mapSubGraph = testMapSubGraph.copy(
      concreteNodes = List(
        makeConcreteNode(hnId1, IoIndex(101L)),
        makeConcreteNode(hnId2, IoIndex(102L))
      ),
      abstractNodes = List(),
      edges = List(
        testHiddenEdge.copy(
          sourceId = hnId1,
          targetId = hnId2,
          samples = List(testSampleIndexies.copy(sampleId = sampleId1))
        )
      ),
      skippedSamples = List(),
      loadedSamples = List(makeSampleData(sampleId1))
    )

    lazy val ioValues = mapSubGraph.concreteNodes.map(_.ioValue)

    val mapGraphMock = stub[MapGraphLake[IO]]
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
      data.setMapGraphMock(data.ioValues, data.loadedSamples, data.mapSubGraph)
      data.mapCache.load(data.ioValues.toSet, data.loadedSamples.toSet).asserting(_ mustBe data.mapSubGraph)

    "fail sub graph is failed" in newCase[CaseData]: (n, data) =>
      data.setMapGraphMock(data.ioValues, data.loadedSamples, data.mapSubGraph.copy(concreteNodes = List()))

      data.mapCache
        .load(data.ioValues.toSet, data.loadedSamples.toSet)
        .assertThrowsError[ValidationError](_.getMessage must startWith("Validation failed for MapSubGraph"))

    "fail if superfluous nodes presented" in newCase[CaseData]: (n, data) =>
      val invConNodes = data.mapSubGraph.concreteNodes :+ makeConcreteNode(HnId(-1), IoIndex(-2))
      data.setMapGraphMock(data.ioValues, data.loadedSamples, data.mapSubGraph.copy(concreteNodes = invConNodes))

      data.mapCache
        .load(data.ioValues.toSet, data.loadedSamples.toSet)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Superfluous nodes presented"))

    "fail if abstract nodes none empty" in newCase[CaseData]: (n, data) =>
      val invNode = List(makeAbstractNode(HnId(-3)))
      data.setMapGraphMock(data.ioValues, data.loadedSamples, data.mapSubGraph.copy(abstractNodes = invNode))

      data.mapCache
        .load(data.ioValues.toSet, data.loadedSamples.toSet)
        .assertThrowsError[AssertionError](_.getMessage must startWith("Abstract nodes should not be loaded"))
