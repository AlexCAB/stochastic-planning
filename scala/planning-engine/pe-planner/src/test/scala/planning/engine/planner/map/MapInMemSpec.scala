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
import planning.engine.common.values.io.{IoName, IoValue}
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.state.{MapGraphState, MapIdsCountState, MapInfoState}
import planning.engine.planner.map.test.data.SimpleMemStateTestData
import planning.engine.planner.map.visualization.MapVisualizationLike

class MapInMemSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with SimpleMemStateTestData:
    val visualizationStub = stub[MapVisualizationLike[IO]]
    val mapInMem = MapInMem.init[IO](visualizationStub).unsafeRunSync()

    def setMapVisStab() = visualizationStub.stateUpdated.when(*, *).returns(IO.unit).once()

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

        counts.nextHnId mustBe 1L
        counts.nextSampleId mustBe 3L
        counts.nextHnIndexMap mustBe Map(HnId(2) -> 3, HnId(1) -> 2, HnId(3) -> 2)

  "MapInMem.init(...)" should:
    "initialize in-memory map state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.setMapVisStab()
        data.mapInMem.init(data.testMetadata, data.testInNodes, data.testOutNodes).logValue(tn).await

        val infoState = data.mapInMem.getMapInfo.logValue(tn).await
        val dcgState = data.mapInMem.getMapState.logValue(tn).await
        val idsCountState = data.mapInMem.getIdsCount.logValue(tn).await

        infoState.metadata mustBe data.testMetadata
        infoState.inNodes mustBe data.testInNodes.map(n => n.name -> n).toMap
        infoState.outNodes mustBe data.testOutNodes.map(n => n.name -> n).toMap
        dcgState.isEmpty mustBe true
        idsCountState.isInit mustBe true

    "fail if initialized more than once" in newCase[CaseData]: (tn, data) =>
      data.setMapVisStab()
      data.mapInMem
        .init(data.testMetadata, data.testInNodes, data.testOutNodes)
        .flatMap(_ => data.mapInMem.init(data.testMetadata, data.testInNodes, data.testOutNodes))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("MapInMem is already initialized"))

  "MapInMem.getIoNode(...)" should:
    "get io node by name from in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.visualizationStub.stateUpdated.when(*, *).returns(IO.unit).once()
        data.mapInMem.setMapInfo(data.testMapInfoState).await

        val inNode = data.mapInMem.getIoNode(data.testInNodes.head.name).logValue(tn).await
        val outNode = data.mapInMem.getIoNode(data.testOutNodes.head.name).logValue(tn).await

        inNode mustBe data.testInNodes.head
        outNode mustBe data.testOutNodes.head

    "fail if io node name not found" in newCase[CaseData]: (tn, data) =>
      data.setMapVisStab()
      data.mapInMem
        .init(data.testMetadata, data.testInNodes, data.testOutNodes)
        .flatMap(_ => data.mapInMem.getIoNode(IoName("unknown_node")))
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"IO node with name"))

  "MapInMem.addNewConcreteNodes(...)" should:
    "add new concrete nodes to in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.setMapVisStab()
        data.mapInMem.setMapInfo(data.testMapInfoState).await

        val result = data.mapInMem.addNewConcreteNodes(data.concreteNodesNew).logValue(tn).await
        val state = data.mapInMem.getMapState.logValue(tn).await

        val nodeWithHdId = data.concreteNodesNew.list
          .map(node => result.find((_, nn) => nn == node.name).getOrElse(fail(s"Node not found: $node"))._1 -> node)
          .toMap

        result.keySet mustBe state.graph.allHnIds
        result.values.toSet mustBe data.concreteNodesNew.list.map(_.name).toSet
        state.ioValues mustBe nodeWithHdId.map((id, n) => IoValue(n.ioNodeName, n.valueIndex) -> Set(id))
        state.graph.concreteNodes.map((i, n) => i -> n.name) mustBe nodeWithHdId.map((i, n) => i -> n.name)

  "MapInMem.addNewAbstractNodes(...)" should:
    "add new abstract nodes to in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.setMapVisStab()

        val result = data.mapInMem.addNewAbstractNodes(data.abstractNodesNew).logValue(tn).await
        val state = data.mapInMem.getMapState.logValue(tn).await

        val nodeWithHdId = data.abstractNodesNew.list
          .map(node => result.find((_, nn) => nn == node.name).getOrElse(fail(s"Node not found: $node"))._1 -> node)
          .toMap

        result.keySet mustBe state.graph.allHnIds
        result.values.toSet mustBe data.abstractNodesNew.list.map(_.name).toSet
        state.graph.abstractNodes.map((i, n) => i -> n.name) mustBe nodeWithHdId.map((i, n) => i -> n.name)

  "MapInMem.addNewSamples(...)" should:
    "add new samples to in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.setMapVisStab()
        data.mapInMem.setMapState(data.initialDcgState).await

        val result = data.mapInMem.addNewSamples(data.sampleListNew).logValue(tn).await
        val state = data.mapInMem.getMapState.logValue(tn).await

        result.view.map((_, s) => s.data.name).toSet mustBe data.sampleListNew.list.map(_.name).toSet
        result.keySet mustBe state.graph.allSampleIds

  "MapInMem.findHnIdsByNames(...)" should:
    "find HnIds by names from in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapInMem.setMapState(data.dcgStateFromSubGraph).await

        val expectedResult = data.conNodes
          .groupBy(n => n.name.getOrElse(fail(s"Node name is not defined, n = $n")))
          .map((name, nodes) => name -> nodes.map(_.id).toSet)

        val result = data.mapInMem
          .findHnIdsByNames(expectedResult.keySet)
          .logValue(tn).await

        result mustBe expectedResult

  "MapInMem.getForIoValues(...)" should:
    "get nodes for io values from in-memory state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapInMem.setMapState(data.dcgStateFromSubGraph).await

        val (foundNodes, notFoundValues) = data.mapInMem
          .findForIoValues(data.ioValues.toSet + data.testNotInMap)
          .logValue(tn).await

        foundNodes mustBe data.conDcgNodesMap
        notFoundValues mustBe Set(data.testNotInMap)

  "MapInMem.reset()" should:
    "reset in-memory map state" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapInMem.setMapState(data.dcgStateFromSubGraph).await

        data.mapInMem.reset().await

        val infoState = data.mapInMem.getMapInfo.logValue(tn).await
        val dcgState = data.mapInMem.getMapState.logValue(tn).await
        val idsCountState = data.mapInMem.getIdsCount.logValue(tn).await

        infoState mustBe MapInfoState.empty[IO]
        dcgState mustBe MapGraphState.empty[IO]
        idsCountState mustBe MapIdsCountState.init
