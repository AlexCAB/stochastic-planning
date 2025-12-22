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
| created: 2025-12-15 |||||||||||*/

package planning.engine.planner.map

import cats.effect.IO
import cats.effect.cps.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.test.data.SimpleMemStateTestData

class MapInMemSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with SimpleMemStateTestData:
    val mapInMem = MapInMem[IO]().unsafeRunSync()

  "MapInMem.buildSamples(...)" should:
    "build samples from naw samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val sampleListNewMap = data.sampleListNew.list.map(s => s.name.get -> s).toMap
        val samples = data.mapInMem.buildSamples(data.sampleListNew).logValue(tn).await
        val counts = data.mapInMem.getIdsCount.logValue(tn).await

        samples.foreach(_.data.name mustBe defined)
        samples.map(_.data.name).distinct.size mustBe data.sampleListNew.list.size
        samples.map(_.data.name) mustBe data.sampleListNew.list.map(_.name)

        samples.foreach: sample =>
          val original = sampleListNewMap(sample.data.name.get)
          val gotEdges = sample.edges.map(e => (e.source.hnId, e.target.hnId, e.edgeType))
          val expectedEdges = original.edges.map(e => (e.source, e.target, e.edgeType))

          sample.data.probabilityCount mustBe original.probabilityCount
          sample.data.utility mustBe original.utility
          sample.data.description mustBe original.description
          gotEdges mustBe expectedEdges

        counts.nextHdId mustBe 1L
        counts.nextSampleId mustBe 3L
        counts.nextHnIndexMap mustBe Map(HnId(2) -> 3, HnId(1) -> 2, HnId(3) -> 2)

  "MapInMem.getForIoValues(...)" should:
    "get nodes for io values from in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapInMem.setMapState(data.dcgStateFromSubGraph).await

        val (foundNodes, notFoundValues) = data.mapInMem
          .getForIoValues(data.ioValues.toSet + data.testNotInMap)
          .logValue(tn).await

        foundNodes mustBe data.conDcgNodesMap
        notFoundValues mustBe Set(data.testNotInMap)

  "MapInMem.addNewSamples(...)" should:
    "add new samples to in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapInMem.setMapState(data.initialDcgState).await

        val result = data.mapInMem.addNewSamples(data.sampleListNew).logValue(tn).await
        val state = data.mapInMem.getMapState.logValue(tn).await

        result.view.map((_, s) => s.data.name).toSet mustBe data.sampleListNew.list.map(_.name).toSet
        result.keySet mustBe state.allSampleIds
