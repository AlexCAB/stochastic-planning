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
import planning.engine.planner.map.dcg.samples.DcgSample
//import planning.engine.common.values.io.{IoName, IoValue}
//import planning.engine.common.values.node.HnId
import planning.engine.planner.map.state.{MapGraphState, MapIdsCountState, MapInfoState}
import planning.engine.planner.map.test.data.MapTestData
import planning.engine.planner.map.visualization.MapVisualizationLike

class MapInMemSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with MapTestData:
    val visualizationStub = stub[MapVisualizationLike[IO]]
    val mapInMem = MapInMem.init[IO](visualizationStub).unsafeRunSync()

    def setMapVisStab() = visualizationStub.stateUpdated.when(*, *).returns(IO.unit).once()

  "MapInMem.buildSamples(...)" should:
    "build samples from naw samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val samples: Iterable[DcgSample.Add[IO]] = mapInMem
          .buildSamples(sampleListNew)(dcgStateFromSubGraph)
          .logValue(tn)
          .await

        val sampleListNewMap = sampleListNew.list.map(s => s.name.get -> s).toMap
        val counts = mapInMem.getIdsCount.logValue(tn).await

        samples.foreach(_.sample.data.name mustBe defined)
        samples.map(_.sample.data.name).toSet.size mustBe sampleListNew.list.size
        samples.map(_.sample.data.name) mustBe sampleListNew.list.map(_.name)

        samples.foreach: add =>
          val original = sampleListNewMap(add.sample.data.name.get)
          val gotEdges = add.sample.structure.keys.map(e => (e.src, e.trg, e.asEdgeType))
          val expectedEdges = original.edges.map(e => (hnIdToMnId(e.source), hnIdToMnId(e.target), e.edgeType))

          add.sample.data.probabilityCount mustBe original.probabilityCount
          add.sample.data.utility mustBe original.utility
          add.sample.data.description mustBe original.description
          gotEdges mustBe expectedEdges

        counts.nextMnId mustBe 1L
        counts.nextSampleId mustBe 3L
        counts.nextHnIndexMap mustBe Map(hnIdToMnId(hnId1) -> 2L, hnIdToMnId(hnId2) -> 3L, hnIdToMnId(hnId3) -> 2L)

//  "MapInMem.init(...)" should:
//    "initialize in-memory map state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        setMapVisStab()
//        mapInMem.init(testMetadata, testInNodes, testOutNodes).logValue(tn).await
//
//        val infoState = mapInMem.getMapInfo.logValue(tn).await
//        val dcgState = mapInMem.getMapState.logValue(tn).await
//        val idsCountState = mapInMem.getIdsCount.logValue(tn).await
//
//        infoState.metadata mustBe testMetadata
//        infoState.inNodes mustBe testInNodes.map(n => n.name -> n).toMap
//        infoState.outNodes mustBe testOutNodes.map(n => n.name -> n).toMap
//        dcgState.isEmpty mustBe true
//        idsCountState.isInit mustBe true
//
//    "fail if initialized more than once" in newCase[CaseData]: (tn, data) =>
//      setMapVisStab()
//      mapInMem
//        .init(testMetadata, testInNodes, testOutNodes)
//        .flatMap(_ => mapInMem.init(testMetadata, testInNodes, testOutNodes))
//        .logValue(tn)
//        .assertThrowsError[AssertionError](_.getMessage must include("MapInMem is already initialized"))
//
//  "MapInMem.getIoNode(...)" should:
//    "get io node by name from in-memory state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        visualizationStub.stateUpdated.when(*, *).returns(IO.unit).once()
//        mapInMem.setMapInfo(testMapInfoState).await
//
//        val inNode = mapInMem.getIoNode(testInNodes.head.name).logValue(tn).await
//        val outNode = mapInMem.getIoNode(testOutNodes.head.name).logValue(tn).await
//
//        inNode mustBe testInNodes.head
//        outNode mustBe testOutNodes.head
//
//    "fail if io node name not found" in newCase[CaseData]: (tn, data) =>
//      setMapVisStab()
//      mapInMem
//        .init(testMetadata, testInNodes, testOutNodes)
//        .flatMap(_ => mapInMem.getIoNode(IoName("unknown_node")))
//        .logValue(tn)
//        .assertThrowsError[AssertionError](_.getMessage must include(s"IO node with name"))
//
//  "MapInMem.addNewConcreteNodes(...)" should:
//    "add new concrete nodes to in-memory state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        setMapVisStab()
//        mapInMem.setMapInfo(testMapInfoState).await
//
//        val result = mapInMem.addNewConcreteNodes(concreteNodesNew).logValue(tn).await
//        val state = mapInMem.getMapState.logValue(tn).await
//
//        val nodeWithHdId = concreteNodesNew.list
//          .map(node => result.find((_, nn) => nn == node.name).getOrElse(fail(s"Node not found: $node"))._1 -> node)
//          .toMap
//
//        result.keySet mustBe state.graph.allHnIds
//        result.values.toSet mustBe concreteNodesNew.list.map(_.name).toSet
//        state.ioValues mustBe nodeWithHdId.map((id, n) => IoValue(n.ioNodeName, n.valueIndex) -> Set(id))
//        state.graph.concreteNodes.map((i, n) => i -> n.name) mustBe nodeWithHdId.map((i, n) => i -> n.name)
//
//  "MapInMem.addNewAbstractNodes(...)" should:
//    "add new abstract nodes to in-memory state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        setMapVisStab()
//
//        val result = mapInMem.addNewAbstractNodes(abstractNodesNew).logValue(tn).await
//        val state = mapInMem.getMapState.logValue(tn).await
//
//        val nodeWithHdId = abstractNodesNew.list
//          .map(node => result.find((_, nn) => nn == node.name).getOrElse(fail(s"Node not found: $node"))._1 -> node)
//          .toMap
//
//        result.keySet mustBe state.graph.allHnIds
//        result.values.toSet mustBe abstractNodesNew.list.map(_.name).toSet
//        state.graph.abstractNodes.map((i, n) => i -> n.name) mustBe nodeWithHdId.map((i, n) => i -> n.name)
//
//  "MapInMem.addNewSamples(...)" should:
//    "add new samples to in-memory state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        setMapVisStab()
//        mapInMem.setMapState(initialDcgState).await
//
//        val result = mapInMem.addNewSamples(sampleListNew).logValue(tn).await
//        val state = mapInMem.getMapState.logValue(tn).await
//
//        result.view.map((_, s) => s.data.name).toSet mustBe sampleListNew.list.map(_.name).toSet
//        result.keySet mustBe state.graph.allSampleIds
//
//  "MapInMem.findHnIdsByNames(...)" should:
//    "find HnIds by names from in-memory state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        mapInMem.setMapState(dcgStateFromSubGraph).await
//
//        val expectedResult = conNodes
//          .groupBy(n => n.name.getOrElse(fail(s"Node name is not defined, n = $n")))
//          .map((name, nodes) => name -> nodes.map(_.id).toSet)
//
//        val result = mapInMem
//          .findHnIdsByNames(expectedResult.keySet)
//          .logValue(tn).await
//
//        result mustBe expectedResult
//
//  "MapInMem.findForIoValues(...)" should:
//    "find nodes for io values from in-memory state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        mapInMem.setMapState(dcgStateFromSubGraph).await
//
//        val (foundNodes, notFoundValues) = mapInMem
//          .findForIoValues(ioValues.toSet + testNotInMap)
//          .logValue(tn).await
//
//        foundNodes mustBe conDcgNodesMap
//        notFoundValues mustBe Set(testNotInMap)
//
//  "MapInMem.findActiveAbstractGraph(...)" should:
//    "find simple active abstract graph" in newCase[CaseData]: (tn, data) =>
//      ???
//
//
//  "MapInMem.reset()" should:
//    "reset in-memory map state" in newCase[CaseData]: (tn, data) =>
//      async[IO]:
//        mapInMem.setMapState(dcgStateFromSubGraph).await
//
//        mapInMem.reset().await
//
//        val infoState = mapInMem.getMapInfo.logValue(tn).await
//        val dcgState = mapInMem.getMapState.logValue(tn).await
//        val idsCountState = mapInMem.getIdsCount.logValue(tn).await
//
//        infoState mustBe MapInfoState.empty[IO]
//        dcgState mustBe MapGraphState.empty[IO]
//        idsCountState mustBe MapIdsCountState.init
