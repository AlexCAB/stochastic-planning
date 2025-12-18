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
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.MapTestData
import planning.engine.planner.map.dcg.edges.DcgEdge

class DcgStateSpec extends UnitSpecWithData with MapTestData:

  private class CaseData extends Case:
    def makeKey(sId: HnId, tId: HnId): DcgEdge.Key = testDcgEdgeKey.copy(sourceId = sId, targetId = tId)

    lazy val emptyDcgState: DcgState[IO] = DcgState.init()

    lazy val keys = Set(
      makeKey(HnId(1), HnId(2)),
      makeKey(HnId(1), HnId(3)),
      makeKey(HnId(2), HnId(2))
    )

    lazy val absNodes = List(HnId(1), HnId(2), HnId(3)).map(id => makeAbstractDcgNode(id = id))
    lazy val conNodes = List(HnId(4), HnId(5)).map(id => makeConcreteDcgNode(id = id))

    lazy val stateWithNodes: DcgState[IO] = emptyDcgState
      .addAbstractNodes(absNodes)
      .flatMap(_.addConcreteNodes(conNodes))
      .unsafeRunSync()

  "DcgState.allHnIds" should:
    "return all HnIds" in newCase[CaseData]: (n, data) =>
      val testConcreteNode = makeConcreteDcgNode()
      val testAbstractNode = makeAbstractDcgNode()

      val state = data.emptyDcgState.copy(
        concreteNodes = Map(testConcreteNode.id -> testConcreteNode),
        abstractNodes = Map(testAbstractNode.id -> testAbstractNode)
      )

      async[IO]:
        logInfo(n, s"state: $state").await
        state.allHnIds mustBe Set(testConcreteNode.id, testAbstractNode.id)

  "DcgState.splitKeys(...)" should:
    "split keys" in newCase[CaseData]: (n, data) =>
      def makeEdge(edgeType: EdgeType): DcgEdge[IO] = testDcgEdge.copy(key = testDcgEdge.key.copy(edgeType = edgeType))

      val edgeLink = makeEdge(EdgeType.LINK)
      val edgeThen = makeEdge(EdgeType.THEN)
      val edges = List(edgeLink, edgeThen)

      async[IO]:
        val (linkKeys, thenKeys) = data.emptyDcgState.splitKeys(edges).await
        logInfo(n, s"linkKeys: $linkKeys, thenKeys: $thenKeys").await

        linkKeys mustBe Set(edgeLink.key)
        thenKeys mustBe Set(edgeThen.key)

  "DcgState.makeForward(...)" should:
    "make forward references" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val forward = data.emptyDcgState.makeForward(data.keys)
        logInfo(n, s"forward: $forward").await

        forward mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(3)),
          HnId(2) -> Set(HnId(2))
        )

  "DcgState.makeBackward(...)" should:
    "make backward references" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val backward = data.emptyDcgState.makeBackward(data.keys)
        logInfo(n, s"backward: $backward").await

        backward mustBe Map(
          HnId(2) -> Set(HnId(1), HnId(2)),
          HnId(3) -> Set(HnId(1))
        )

  "DcgState.joinIds(...)" should:
    "join IDs references" in newCase[CaseData]: (n, data) =>
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
        logInfo(n, s"joined: $joined").await

        joined mustBe Map(
          HnId(1) -> Set(HnId(2), HnId(4)),
          HnId(2) -> Set(HnId(3)),
          HnId(3) -> Set(HnId(5))
        )

    "fail if duplicate references" in newCase[CaseData]: (n, data) =>
      val oldIds = Map(HnId(1) -> Set(HnId(2)))
      val newIds = Map(HnId(1) -> Set(HnId(2)))

      data.emptyDcgState.joinIds(oldIds, newIds).logValue(n).assertThrows[AssertionError]

  "DcgState.addConcreteNodes(...)" should:
    val List(n1, n2, n3) = List((1, 101), (2, 101), (3, 102)).map:
      case (id, ioIdx) => makeConcreteDcgNode(id = HnId(id), valueIndex = IoIndex(ioIdx))

    "add concrete nodes" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val state = data.emptyDcgState.addConcreteNodes(List(n1, n2, n3)).await
        logInfo(n, s"state: $state").await

        state.ioValues mustBe Map(n1.ioValue -> Set(n1.id, n2.id), n3.ioValue -> Set(n3.id))
        state.concreteNodes mustBe List(n1, n2, n3).map(n => n.id -> n).toMap

    "fail if node IDs is not distinct" in newCase[CaseData]: (n, data) =>
      data.emptyDcgState.addConcreteNodes(List(n1, n1)).logValue(n).assertThrows[AssertionError]

    "fail if ioValues already exist" in newCase[CaseData]: (n, data) =>
      data
        .emptyDcgState.addConcreteNodes(List(n1))
        .flatMap(stateWithN1 => stateWithN1.addConcreteNodes(List(n2)))
        .logValue(n)
        .assertThrows[AssertionError]

    "fail if concrete nodes that already exist" in newCase[CaseData]: (n, data) =>
      data
        .emptyDcgState.copy(concreteNodes = Map(n1.id -> n1))
        .addConcreteNodes(List(n1))
        .logValue(n)
        .assertThrows[AssertionError]

    "DcgState.addAbstractNodes(...)" should:
      val nodes = List(1, 2, 3).map(id => makeAbstractDcgNode(id = HnId(id)))

      "add abstract nodes" in newCase[CaseData]: (n, data) =>
        async[IO]:
          val state = data.emptyDcgState.addAbstractNodes(nodes).await
          logInfo(n, s"state: $state").await

          state.abstractNodes mustBe nodes.map(n => n.id -> n).toMap

      "fail if node IDs is not distinct" in newCase[CaseData]: (n, data) =>
        data.emptyDcgState.addAbstractNodes(List(nodes.head, nodes.head)).logValue(n).assertThrows[AssertionError]

      "fail if abstract nodes that already exist" in newCase[CaseData]: (n, data) =>
        data
          .emptyDcgState.copy(abstractNodes = Map(n1.id -> nodes.head))
          .addAbstractNodes(List(nodes.head))
          .logValue(n)
          .assertThrows[AssertionError]

  "DcgState.addEdges(...)" should:
    val List(e1, e2, e3, e4) = List(
        DcgEdge.Key(EdgeType.LINK, HnId(1), HnId(2)),
        DcgEdge.Key(EdgeType.THEN, HnId(2), HnId(3)),
        DcgEdge.Key(EdgeType.LINK, HnId(1), HnId(4)),
        DcgEdge.Key(EdgeType.THEN, HnId(4), HnId(5))
      ).map[DcgEdge[IO]](k => testDcgEdge.copy(key = k))

    "add edges" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val state = data.stateWithNodes.addEdges(List(e1, e2, e3, e4)).await
        logInfo(n, s"state: $state").await

        state.edges mustBe List(e1, e2, e3, e4).map(e => e.key -> e).toMap
        state.forwardLinks mustBe Map(HnId(1) -> Set(HnId(4), HnId(2)))
        state.backwardLinks mustBe Map(HnId(4) -> Set(HnId(1)), HnId(2) -> Set(HnId(1)))
        state.forwardThen mustBe Map(HnId(4) -> Set(HnId(5)), HnId(2) -> Set(HnId(3)))
        state.backwardThen mustBe Map(HnId(3) -> Set(HnId(2)), HnId(5) -> Set(HnId(4)))

    "fail if edge keys is not distinct" in newCase[CaseData]: (n, data) =>
      data.stateWithNodes.addEdges(List(e1, e1)).logValue(n).assertThrows[AssertionError]

    "fail if edges connect to non existing nodes" in newCase[CaseData]: (n, data) =>
      data.emptyDcgState.addEdges(List(e1)).logValue(n).assertThrows[AssertionError]

    "fail if edge already exist" in newCase[CaseData]: (n, data) =>
      data.stateWithNodes.addEdges(List(e1)).flatMap(_.addEdges(List(e1))).logValue(n).assertThrows[AssertionError]

  "DcgState.addSamples(...)" should:
    val List(s1, s2, s3) = List(1, 2, 3).map(id => makeSampleData(id = SampleId(id)))

    "add samples" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val state = data.emptyDcgState.addSamples(List(s1, s2, s3)).await
        logInfo(n, s"state: $state").await

        state.samplesData mustBe List(s1, s2, s3).map(s => s.id -> s).toMap

    "fail if sample IDs is not distinct" in newCase[CaseData]: (n, data) =>
      data.emptyDcgState.addSamples(List(s1, s1)).logValue(n).assertThrows[AssertionError]

    "fail if samples that already exist" in newCase[CaseData]: (n, data) =>
      data.emptyDcgState.addSamples(List(s1)).flatMap(_.addSamples(List(s1))).logValue(n).assertThrows[AssertionError]

  "DcgState.concreteForHnId(...)" should:
    "get concrete node for HnId" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val conNode = data.stateWithNodes.concreteForHnId(data.conNodes.head.id).await
        logInfo(n, s"conNode: $conNode").await

        conNode mustBe data.conNodes.head

    "fail if concrete node for HnId not found" in newCase[CaseData]: (n, data) =>
      data.stateWithNodes.concreteForHnId(HnId(-1)).logValue(n).assertThrows[AssertionError]

  "DcgState.abstractForHnId(...)" should:
    "get abstract node for HnId" in newCase[CaseData]: (n, data) =>
      async[IO]:
        val absNode = data.stateWithNodes.abstractForHnId(data.absNodes.head.id).await
        logInfo(n, s"absNode: $absNode").await

        absNode mustBe data.absNodes.head

    "fail if abstract node for HnId not found:" in newCase[CaseData]: (n, data) =>
      data.stateWithNodes.abstractForHnId(HnId(-1)).logValue(n).assertThrows[AssertionError]

  "DcgState.concreteForIoValues(...)" should:
    "get concrete nodes for IoValues" in newCase[CaseData]: (n, data) =>
      val ioValue1 = data.conNodes.head.ioValue
      val ioValue2 = IoValue(IoName("not_in_set"), IoIndex(123))

      async[IO]:
        val (map, notFound) = data.stateWithNodes.concreteForIoValues(Set(ioValue1, ioValue2)).await
        logInfo(n, s"map: $map, notFound: $notFound").await

        map mustBe Map(ioValue1 -> data.conNodes.filter(_.ioValue == ioValue1).toSet)
        notFound mustBe Set(ioValue2)
