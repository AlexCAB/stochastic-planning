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
| created: 2025-12-13 |||||||||||*/

package planning.engine.planner.map.dcg.state

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.test.data.{MapDcgNodeTestData, MapSampleTestData}
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.edges.DcgEdge.Indexies

class DcgStateSpec extends UnitSpecWithData:

  private class CaseData extends Case with MapDcgNodeTestData with MapSampleTestData:
    def makeKey(sId: HnId, tId: HnId, edgeType: EdgeType = EdgeType.LINK): DcgEdge.Key =
      DcgEdge.Key(edgeType, sourceId = sId, targetId = tId)

    lazy val emptyDcgState: DcgState[IO] = DcgState.empty[IO]

    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)
    lazy val hnId4 = HnId(4)
    lazy val hnId5 = HnId(5)
    lazy val nuHnId = HnId(-1)

    lazy val keys = Set(
      makeKey(hnId1, hnId2),
      makeKey(hnId1, hnId3),
      makeKey(hnId2, hnId2)
    )

    lazy val absNodes = List(hnId1, hnId2, hnId3).map(id => makeAbstractDcgNode(id = id))
    lazy val conNodes = List(hnId4, hnId5).map(id => makeConcreteDcgNode(id = id))

    lazy val stateWithNodes: DcgState[IO] = emptyDcgState
      .addAbstractNodes(absNodes)
      .flatMap(_.addConcreteNodes(conNodes))
      .unsafeRunSync()

    lazy val sampleId1 = SampleId(1001)
    lazy val sampleId2 = SampleId(1002)

    def makeIndexiesMap(sId: SampleId): Map[HnId, HnIndex] = List(hnId1, hnId2, hnId3, hnId4, hnId5, nuHnId)
      .zipWithIndex.map((id, i) => id -> HnIndex(100 + sId.value + i + 1)).toMap

    def makeSampleRecord(sId: SampleId, snId: HnId, tnId: HnId): (SampleId, Indexies) =
      val ixMap = makeIndexiesMap(sId)
      sId -> Indexies(
        sourceIndex = ixMap.getOrElse(snId, fail(s"Source HnId index not found, for $snId")),
        targetIndex = ixMap.getOrElse(tnId, fail(s"Target HnId index not found, for $tnId"))
      )

    def makeDcgEdge(snId: HnId, tnId: HnId, sIds: List[SampleId], et: EdgeType = EdgeType.LINK): DcgEdge[IO] =
      DcgEdge[IO](
        makeKey(snId, tnId, et),
        sIds.map(sId => makeSampleRecord(sId, snId, tnId)).toMap
      )

    lazy val dcgEdges = List(
      makeDcgEdge(hnId1, hnId2, List(sampleId1, sampleId2), EdgeType.LINK),
      makeDcgEdge(hnId2, hnId3, List(sampleId1), EdgeType.LINK),
      makeDcgEdge(hnId2, hnId4, List(sampleId2), EdgeType.THEN),
      makeDcgEdge(hnId2, hnId5, List(sampleId2), EdgeType.THEN)
    )

    lazy val dcgEdge = dcgEdges.head
    lazy val stateWithEdges = stateWithNodes.addEdges(dcgEdges).unsafeRunSync()

    lazy val List(n1, n2, n3) = List((1, 101), (2, 101), (3, 102)).map:
      case (id, ioIdx) => makeConcreteDcgNode(id = HnId(id), valueIndex = IoIndex(ioIdx))

    lazy val List(s1, s2, s3) = List(1, 2, 3).map(id => makeSampleData(id = SampleId(id)))
    lazy val nodes = List(1, 2, 3).map(id => makeAbstractDcgNode(id = HnId(id)))

  "DcgState.allHnIds" should:
    "return all HnIds" in newCase[CaseData]: (tn, data) =>
      val testConcreteNode = data.makeConcreteDcgNode()
      val testAbstractNode = data.makeAbstractDcgNode()

      val state = data.emptyDcgState.copy(
        concreteNodes = Map(testConcreteNode.id -> testConcreteNode),
        abstractNodes = Map(testAbstractNode.id -> testAbstractNode)
      )

      async[IO]:
        logInfo(tn, s"state: $state").await
        state.allHnIds mustBe Set(testConcreteNode.id, testAbstractNode.id)

  "DcgState.allSampleIds" should:
    "return all SampleIds" in newCase[CaseData]: (tn, data) =>
      val testSample1 = data.makeSampleData(SampleId(5001))
      val testSample2 = data.makeSampleData(SampleId(5002))

      val state = data.emptyDcgState
        .copy(samplesData = Map(testSample1.id -> testSample1, testSample2.id -> testSample2))

      async[IO]:
        logInfo(tn, s"state: $state").await
        state.allSampleIds mustBe Set(testSample1.id, testSample2.id)

  "DcgState.isEmpty" should:
    "return true for empty state" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.pure[IO].asserting(_.isEmpty mustBe true)

    "return false for non empty state" in newCase[CaseData]: (tn, data) =>
      data.stateWithNodes.pure[IO].asserting(_.isEmpty mustBe false)

  "DcgState.checkEdges(...)" should:
    "check edges and return no error for valid" in newCase[CaseData]: (tn, data) =>
      data.stateWithNodes.checkEdges(data.dcgEdges).logValue(tn).assertNoException

    "fail if duplicate edge keys" in newCase[CaseData]: (tn, data) =>
      data.stateWithNodes.checkEdges(data.dcgEdges :+ data.dcgEdges.head).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate Edge Keys detected"))

    "fail if edge connects to non existing hidden nodes" in newCase[CaseData]: (tn, data) =>
      val invalidEdge = data.makeDcgEdge(data.nuHnId, data.nuHnId, List(data.sampleId1))
      data.stateWithNodes.checkEdges(data.dcgEdges :+ invalidEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Edge refers to unknown HnIds"))

    "fail if duplicate sample IDs for edge between same HnIds" in newCase[CaseData]: (tn, data) =>
      val duplicateSampleEdge = data.makeDcgEdge(data.hnId1, data.hnId2, List(data.sampleId1), EdgeType.THEN)
      data.stateWithNodes.checkEdges(data.dcgEdges :+ duplicateSampleEdge).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate SampleIds for Edge between HnIds"))

  "DcgState.splitKeys(...)" should:
    "split keys" in newCase[CaseData]: (tn, data) =>
      def makeEdge(edgeType: EdgeType): DcgEdge[IO] =
        data.dcgEdge.copy(key = data.dcgEdge.key.copy(edgeType = edgeType))

      val edgeLink = makeEdge(EdgeType.LINK)
      val edgeThen = makeEdge(EdgeType.THEN)
      val edges = List(edgeLink, edgeThen)

      async[IO]:
        val (linkKeys, thenKeys) = data.emptyDcgState.splitKeys(edges).await
        logInfo(tn, s"linkKeys: $linkKeys, thenKeys: $thenKeys").await

        linkKeys mustBe Set(edgeLink.key)
        thenKeys mustBe Set(edgeThen.key)

  "DcgState.makeForward(...)" should:
    "make forward references" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val forward = data.emptyDcgState.makeForward(data.keys)
        logInfo(tn, s"forward: $forward").await

        forward mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(3)),
          HnId(2) -> Set(HnId(2))
        )

  "DcgState.makeBackward(...)" should:
    "make backward references" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val backward = data.emptyDcgState.makeBackward(data.keys)
        logInfo(tn, s"backward: $backward").await

        backward mustBe Map(
          HnId(2) -> Set(HnId(1), HnId(2)),
          HnId(3) -> Set(HnId(1))
        )

  "DcgState.joinIds(...)" should:
    "join IDs references" in newCase[CaseData]: (tn, data) =>
      val oldIds = Map(
        HnId(1) -> Set(HnId(2)),
        HnId(2) -> Set(HnId(3))
      )
      val newIds = Map(
        HnId(1) -> Set(HnId(4)),
        HnId(3) -> Set(HnId(5))
      )

      async[IO]:
        val joined = data.emptyDcgState.joinIds(oldIds, newIds).await
        logInfo(tn, s"joined: $joined").await

        joined mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(4)),
          HnId(2) -> Set(HnId(3)),
          HnId(3) -> Set(HnId(5))
        )

    "fail if duplicate references" in newCase[CaseData]: (tn, data) =>
      val oldIds = Map(HnId(1) -> Set(HnId(2)))
      val newIds = Map(HnId(1) -> Set(HnId(2)))

      data.emptyDcgState.joinIds(oldIds, newIds).logValue(tn).assertThrows[AssertionError]

  "DcgState.addConcreteNodes(...)" should:
    "add concrete nodes" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val state = data.emptyDcgState.addConcreteNodes(List(data.n1, data.n2, data.n3)).await
        logInfo(tn, s"state: $state").await

        state.ioValues mustBe Map(data.n1.ioValue -> Set(data.n1.id, data.n2.id), data.n3.ioValue -> Set(data.n3.id))
        state.concreteNodes mustBe List(data.n1, data.n2, data.n3).map(n => n.id -> n).toMap

    "fail if node IDs is not distinct" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.addConcreteNodes(List(data.n1, data.n1)).logValue(tn).assertThrows[AssertionError]

    "fail if ioValues already exist" in newCase[CaseData]: (tn, data) =>
      data
        .emptyDcgState.addConcreteNodes(List(data.n1))
        .flatMap(stateWithN1 => stateWithN1.addConcreteNodes(List(data.n2)))
        .logValue(tn)
        .assertThrows[AssertionError]

    "fail if concrete nodes that already exist" in newCase[CaseData]: (tn, data) =>
      data
        .emptyDcgState.copy(concreteNodes = Map(data.n1.id -> data.n1))
        .addConcreteNodes(List(data.n1))
        .logValue(tn)
        .assertThrows[AssertionError]

    "DcgState.addAbstractNodes(...)" should:
      "add abstract nodes" in newCase[CaseData]: (tn, data) =>
        async[IO]:
          val state = data.emptyDcgState.addAbstractNodes(data.nodes).await
          logInfo(tn, s"state: $state").await

          state.abstractNodes mustBe data.nodes.map(n => n.id -> n).toMap

      "fail if node IDs is not distinct" in newCase[CaseData]: (tn, data) =>
        data.emptyDcgState.addAbstractNodes(List(data.nodes.head, data.nodes.head)).logValue(tn)
          .assertThrows[AssertionError]

      "fail if abstract nodes that already exist" in newCase[CaseData]: (tn, data) =>
        data
          .emptyDcgState.copy(abstractNodes = Map(data.n1.id -> data.nodes.head))
          .addAbstractNodes(List(data.nodes.head))
          .logValue(tn)
          .assertThrows[AssertionError]

  "DcgState.addEdges(...)" should:
    "add edges" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val state = data.stateWithNodes.addEdges(data.dcgEdges).await
        logInfo(tn, s"state: $state").await

        state.edges mustBe data.dcgEdges.map(e => e.key -> e).toMap
        state.forwardLinks mustBe Map(data.hnId2 -> Set(data.hnId3), data.hnId1 -> Set(data.hnId2))
        state.backwardLinks mustBe Map(data.hnId3 -> Set(data.hnId2), data.hnId2 -> Set(data.hnId1))
        state.forwardThen mustBe Map(data.hnId2 -> Set(data.hnId5, data.hnId4))
        state.backwardThen mustBe Map(data.hnId4 -> Set(data.hnId2), data.hnId5 -> Set(data.hnId2))

    "fail if edge keys is not distinct" in newCase[CaseData]: (tn, data) =>
      val e1 = data.dcgEdges.head
      data.stateWithNodes.addEdges(List(e1, e1)).logValue(tn).assertThrows[AssertionError]

    "fail if edges connect to non existing nodes" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.addEdges(List(data.dcgEdges.head)).logValue(tn).assertThrows[AssertionError]

    "fail if edge already exist" in newCase[CaseData]: (tn, data) =>
      val e1 = data.dcgEdges.head
      data.stateWithNodes.addEdges(List(e1)).flatMap(_.addEdges(List(e1))).logValue(tn).assertThrows[AssertionError]

  "DcgState.mergeEdges(...)" should:
    lazy val sampleId3 = SampleId(1003)
    lazy val sampleId4 = SampleId(1004)

    "merge edges" in newCase[CaseData]: (tn, data) =>
      lazy val dcgEdges2 = List(
        data.makeDcgEdge(data.hnId1, data.hnId2, List(sampleId3, sampleId4), EdgeType.THEN),
        data.makeDcgEdge(data.hnId2, data.hnId3, List(sampleId3), EdgeType.LINK),
        data.makeDcgEdge(data.hnId1, data.hnId5, List(sampleId4), EdgeType.THEN)
      )

      async[IO]:
        val joinedEdges = (data.dcgEdges ++ dcgEdges2)
          .groupBy(_.key).view.mapValues(_.reduceLeft((e1, e2) => e1.join(e2).unsafeRunSync()))
          .toMap

        val state = data.stateWithEdges.mergeEdges(dcgEdges2).await
        logInfo(tn, s"state: $state").await

        state.edges mustBe joinedEdges
        state.forwardLinks mustBe Map(data.hnId2 -> Set(data.hnId3), data.hnId1 -> Set(data.hnId2))
        state.backwardLinks mustBe Map(data.hnId3 -> Set(data.hnId2), data.hnId2 -> Set(data.hnId1))

        state.forwardThen mustBe Map(
          data.hnId2 -> Set(data.hnId5, data.hnId4),
          data.hnId1 -> Set(data.hnId5, data.hnId2)
        )

        state.backwardThen mustBe Map(
          data.hnId4 -> Set(data.hnId2),
          data.hnId5 -> Set(data.hnId2, data.hnId1),
          data.hnId2 -> Set(data.hnId1)
        )

  "DcgState.addSamples(...)" should:
    "add samples" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val state = data.emptyDcgState.addSamples(List(data.s1, data.s2, data.s3)).await
        logInfo(tn, s"state: $state").await

        state.samplesData mustBe List(data.s1, data.s2, data.s3).map(s => s.id -> s).toMap

    "fail if sample IDs is not distinct" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.addSamples(List(data.s1, data.s1)).logValue(tn).assertThrows[AssertionError]

    "fail if samples that already exist" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.addSamples(List(data.s1)).flatMap(_.addSamples(List(data.s1)))
        .logValue(tn).assertThrows[AssertionError]

  "DcgState.concreteForHnId(...)" should:
    "get concrete node for HnId" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val conNode = data.stateWithNodes.concreteForHnId(data.conNodes.head.id).await
        logInfo(tn, s"conNode: $conNode").await

        conNode mustBe data.conNodes.head

    "fail if concrete node for HnId not found" in newCase[CaseData]: (tn, data) =>
      data.stateWithNodes.concreteForHnId(HnId(-1)).logValue(tn).assertThrows[AssertionError]

  "DcgState.abstractForHnId(...)" should:
    "get abstract node for HnId" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val absNode = data.stateWithNodes.abstractForHnId(data.absNodes.head.id).await
        logInfo(tn, s"absNode: $absNode").await

        absNode mustBe data.absNodes.head

    "fail if abstract node for HnId not found:" in newCase[CaseData]: (tn, data) =>
      data.stateWithNodes.abstractForHnId(HnId(-1)).logValue(tn).assertThrows[AssertionError]

  "DcgState.concreteForIoValues(...)" should:
    "get concrete nodes for IoValues" in newCase[CaseData]: (tn, data) =>
      val ioValue1 = data.conNodes.head.ioValue
      val ioValue2 = IoValue(IoName("not_in_set"), IoIndex(123))

      async[IO]:
        val (map, notFound) = data.stateWithNodes.concreteForIoValues(Set(ioValue1, ioValue2)).await
        logInfo(tn, s"map: $map, notFound: $notFound").await

        map mustBe Map(ioValue1 -> data.conNodes.filter(_.ioValue == ioValue1).toSet)
        notFound mustBe Set(ioValue2)

  "DcgState.findHnIdsByNames(...)" should:
    "find HnIds by names" in newCase[CaseData]: (tn, data) =>
      val name1 = data.absNodes.head.name.get
      val name2 = HnName("unknown_name")

      async[IO]:
        val result = data.stateWithNodes.findHnIdsByNames(Set(name1, name2)).await
        logInfo(tn, s"result: $result").await

        result mustBe Map(name1 -> data.absNodes.filter(_.name.contains(name1)).map(_.id), name2 -> List())
