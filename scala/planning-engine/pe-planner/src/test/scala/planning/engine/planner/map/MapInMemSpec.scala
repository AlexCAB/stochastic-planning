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
import planning.engine.common.values.node.{HnIndex, HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.planner.map.state.{MapGraphState, MapIdsCountState, MapInfoState}
import planning.engine.planner.map.test.data.MapTestData
import planning.engine.planner.map.visualization.MapVisualizationLike

class MapInMemSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with MapTestData:
    lazy val visualizationStub = stub[MapVisualizationLike[IO]]

    lazy val emptyMapInMem: MapInMem[IO] =
      visualizationStub.stateUpdated.when(*, *).returns(IO.unit).once()
      MapInMem.empty[IO](visualizationStub).unsafeRunSync()

    lazy val initMapInMem: MapInMem[IO] =
      visualizationStub.stateUpdated.when(*, *).returns(IO.unit).anyNumberOfTimes()
      MapInMem.empty[IO](visualizationStub)
        .flatTap(_.init(testMetadata, testInNodes, testOutNodes))
        .flatTap(_.setIdsCount(initialMapIdsCountState))
        .flatTap(_.setMapState(initialDcgState))
        .unsafeRunSync()

  "MapInMem.buildSamples(...)" should:
    "build samples from naw samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val samples: Iterable[DcgSample.Add[IO]] = emptyMapInMem
          .buildSamples(sampleListNew)(dcgStateFromSubGraph)
          .logValue(tn)
          .await

        val sampleListNewMap = sampleListNew.list.map(s => s.name.get -> s).toMap
        val counts = emptyMapInMem.getIdsCount.logValue(tn).await

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

  "MapInMem.init(...)" should:
    "initialize in-memory map state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        emptyMapInMem.init(testMetadata, testInNodes, testOutNodes).logValue(tn).await

        val infoState = emptyMapInMem.getMapInfo.logValue(tn).await
        val dcgState = emptyMapInMem.getMapState.logValue(tn).await
        val idsCountState = emptyMapInMem.getIdsCount.logValue(tn).await

        infoState.metadata mustBe testMetadata
        infoState.inNodes mustBe testInNodes.map(n => n.name -> n).toMap
        infoState.outNodes mustBe testOutNodes.map(n => n.name -> n).toMap
        dcgState.isEmpty mustBe true
        idsCountState.isInit mustBe true

    "fail if initialized more than once" in newCase[CaseData]: (tn, data) =>
      import data.*
      initMapInMem
        .init(testMetadata, testInNodes, testOutNodes)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("MapInMem is already initialized"))

  "MapInMem.getIoNode(...)" should:
    "get io node by name from in-memory state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val inNode = initMapInMem.getIoNode(testInNodes.head.name).logValue(tn).await
        val outNode = initMapInMem.getIoNode(testOutNodes.head.name).logValue(tn).await

        inNode mustBe testInNodes.head
        outNode mustBe testOutNodes.head

    "fail if io node name not found" in newCase[CaseData]: (tn, data) =>
      import data.*
      initMapInMem.getIoNode(IoName("unknown_node")).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"IO node with name"))

  "MapInMem.addNewConcreteNodes(...)" should:
    "add new concrete nodes to in-memory state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val result: Map[MnId, Option[HnName]] = initMapInMem.addNewConcreteNodes(concreteNodesNew).logValue(tn).await
        val state: MapGraphState[IO] = initMapInMem.getMapState.logValue(tn).await

        val nodeWithHdId: List[(MnId, ConcreteNode.New)] = concreteNodesNew.list
          .map(node => result.find((_, nn) => nn == node.name).getOrElse(fail(s"Node not found: $node"))._1 -> node)

        state.graph.mnIds must contain allElementsOf result.keySet
        result.values.toSet mustBe concreteNodesNew.list.map(_.name).toSet

        nodeWithHdId.foreach: (newMnId, conNode) =>
          val ioVal = IoValue(conNode.ioNodeName, conNode.valueIndex)
          val associatedMnIds = state.ioValues.valueMap.getOrElse(ioVal, fail(s"IoValue not found in state: $ioVal"))
          associatedMnIds must contain(newMnId)

        val allMnIdWithNameMap = state.graph.nodes.map((i, n) => i -> n.name)
        val newMnIdWithNameMap = nodeWithHdId.map((i, n) => i -> n.name).toMap

        allMnIdWithNameMap must contain allElementsOf newMnIdWithNameMap

  "MapInMem.addNewAbstractNodes(...)" should:
    "add new abstract nodes to in-memory state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val result = initMapInMem.addNewAbstractNodes(abstractNodesNew).logValue(tn).await
        val state = initMapInMem.getMapState.logValue(tn).await

        val nodeWithHdId = abstractNodesNew.list
          .map(node => result.find((_, nn) => nn == node.name).getOrElse(fail(s"Node not found: $node"))._1 -> node)
          .toMap

        state.graph.mnIds must contain allElementsOf result.keySet
        result.values.toSet mustBe abstractNodesNew.list.map(_.name).toSet

        val allMnIdWithNameMap = state.graph.nodes.map((i, n) => i -> n.name)
        val newMnIdWithNameMap = nodeWithHdId.map((i, n) => i -> n.name).toMap

        allMnIdWithNameMap must contain allElementsOf newMnIdWithNameMap

  "MapInMem.buildDcgSampleAdd(...)" should:
    "build DcgSample.Add from new sample" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val original = sampleListNew.list.head

        val result: DcgSample.Add[IO] = emptyMapInMem.buildDcgSampleAdd(sampleId1, original, initialDcgState)
          .logValue(tn)
          .await

        result.sample.data.name mustBe original.name
        result.sample.data.probabilityCount mustBe original.probabilityCount
        result.sample.data.utility mustBe original.utility
        result.sample.data.description mustBe original.description

        val gotEdges = result.sample.structure.keys.map(e => (e.src.asHnId, e.trg.asHnId, e.asEdgeType))
        val expectedEdges = original.edges.map(e => (e.source, e.target, e.edgeType))

        gotEdges mustBe expectedEdges

        val gotHnId = result.indexMap.indexies.keySet.map(_.asHnId)
        val expectedHnId = original.edges.flatMap(e => Set(e.source, e.target))

        gotHnId mustBe Set(hnId1, hnId2)
        gotHnId mustBe expectedHnId

        // For both new nodes generated HnIndex(1) because  initial DcgState is empty
        result.indexMap.indexies.values.toSet mustBe Set(HnIndex(1))

  "MapInMem.buildSamples(...)" should:
    "build DcgSample.Add for all new samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val result: Iterable[DcgSample.Add[IO]] = emptyMapInMem
          .buildSamples(sampleListNew)(initialDcgState)
          .logValue(tn)
          .await

        val sampleListNewMap = sampleListNew.list.map(s => s.name.get -> s).toMap

        result.foreach: add =>
          val original = sampleListNewMap(add.sample.data.name.get)
          val gotEdges = add.sample.structure.keys.map(e => (e.src.asHnId, e.trg.asHnId, e.asEdgeType))
          val expectedEdges = original.edges.map(e => (e.source, e.target, e.edgeType))

          add.sample.data.probabilityCount mustBe original.probabilityCount
          add.sample.data.utility mustBe original.utility
          add.sample.data.description mustBe original.description
          gotEdges mustBe expectedEdges
          add.indexMap.indexies.keySet mustBe add.sample.structure.mnIds

        result.foreach(_.sample.data.name mustBe defined)
        result.map(_.sample.data.name).toSet.size mustBe sampleListNew.list.size
        result.map(_.sample.data.name) mustBe sampleListNew.list.map(_.name)

  "MapInMem.addNewSamples(...)" should:
    "add new samples to in-memory state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val result: Map[SampleId, DcgSample[IO]] = initMapInMem.addNewSamples(sampleListNew).logValue(tn).await
        val state: MapGraphState[IO] = initMapInMem.getMapState.logValue(tn).await

        result.view.map((_, s) => s.data.name).toSet mustBe sampleListNew.list.map(_.name).toSet
        result.keySet mustBe state.graph.sampleIds

  "MapInMem.findHnIdsByNames(...)" should:
    "find HnIds by names from in-memory state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val expectedResult = conDcgNodes
          .groupBy(n => n.name.getOrElse(fail(s"Node name is not defined, n = $n")))
          .map((name, nodes) => name -> nodes.map(_.id).toSet)

        logInfo(tn, s"Expected result: $expectedResult").await

        val result = initMapInMem
          .findHnIdsByNames(expectedResult.keySet)
          .logValue(tn, "result").await

        result mustBe expectedResult

  "MapInMem.findForIoValues(...)" should:
    "find nodes for io values from in-memory state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val (foundNodes, notFoundValues) = initMapInMem
          .findForIoValues(ioValues.toSet + testNotInMap)
          .logValue(tn).await

        foundNodes mustBe conDcgNodesMap
        notFoundValues mustBe Set(testNotInMap)

// TODO To implement:

//  "MapInMem.findActiveAbstractGraph(...)" should:
//    "find simple active abstract graph" in newCase[CaseData]: (tn, data) =>
//      ???

  "MapInMem.reset()" should:
    "reset in-memory map state" in newCase[CaseData]: (tn, data) =>
      import data.initMapInMem
      async[IO]:
        initMapInMem.reset().await

        val infoState = initMapInMem.getMapInfo.logValue(tn).await
        val dcgState = initMapInMem.getMapState.logValue(tn).await
        val idsCountState = initMapInMem.getIdsCount.logValue(tn).await

        infoState mustBe MapInfoState.empty[IO]
        dcgState mustBe MapGraphState.empty[IO]
        idsCountState mustBe MapIdsCountState.init
  
  "MapInMem.empty(...)" should:
    "create empty MapInMem instance" in newCase[CaseData]: (tn, data) =>
      import data.visualizationStub
      async[IO]:
        val mapInMem = MapInMem.empty[IO](visualizationStub).logValue(tn).await

        mapInMem.getMapInfo.logValue(tn).await mustBe MapInfoState.empty[IO]
        mapInMem.getMapState.logValue(tn).await mustBe MapGraphState.empty[IO]
        mapInMem.getIdsCount.logValue(tn).await mustBe MapIdsCountState.init
