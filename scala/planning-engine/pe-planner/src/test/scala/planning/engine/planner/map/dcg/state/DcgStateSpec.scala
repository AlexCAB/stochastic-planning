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
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.HnId
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
      def makeEdge(edgeType: EdgeType): DcgEdge = testDcgEdge.copy(key = testDcgEdge.key.copy(edgeType = edgeType))

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

    "fail if duplicate references" in newCase[CaseData]: (_, data) =>
      val oldIds = Map(HnId(1) -> Set(HnId(2)))
      val newIds = Map(HnId(1) -> Set(HnId(2)))

      data.emptyDcgState.joinIds(oldIds, newIds).assertThrows[AssertionError]

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
      data.emptyDcgState.addConcreteNodes(List(n1, n1)).assertThrows[AssertionError]

    "fail if ioValues already exist" in newCase[CaseData]: (n, data) =>
      data
        .emptyDcgState.addConcreteNodes(List(n1))
        .flatMap(stateWithN1 => stateWithN1.addConcreteNodes(List(n2)))
        .assertThrows[AssertionError]

    "fail if concrete nodes that already exist" in newCase[CaseData]: (n, data) =>
      data
        .emptyDcgState.copy(concreteNodes = Map(n1.id -> n1))
        .addConcreteNodes(List(n1))
        .assertThrows[AssertionError]