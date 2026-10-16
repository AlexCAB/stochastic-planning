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
| created: 2026-10-11 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.parts

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.{EdgeData, Lookup}
import planning.engine.planner.mpi.test.data.NodeTestData

class LookupSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val sId1: SampleId = SampleId(1L)
    val sId2: SampleId = SampleId(2L)
    val sId3: SampleId = SampleId(3L)
    val unknownSId: SampleId = SampleId(99L)
    val allSIds: Set[SampleId] = Set(sId1, sId2, sId3)

    val node1: Node = makeNodeStub(MnId.Con(1L))
    val node2: Node = makeNodeStub(MnId.Abs(2L))
    val node3: Node = makeNodeStub(MnId.Abs(3L))
    val node4: Node = makeNodeStub(MnId.Abs(4L))
    val node5: Node = makeNodeStub(MnId.Abs(5L))

    def edge(neighbor: Node, et: EdgeType, sampleIds: SampleId*): (MnId, EdgeData) =
      neighbor.mnId -> EdgeData(neighbor, sampleIds.toSet, et)

    // Each edge group has distinct content, to make sure each method use the correct edges.
    val lookup: Lookup = Lookup(
      incoming = Map(
        edge(node1, EdgeType.LINK, sId1, sId2),
        edge(node2, EdgeType.LINK, sId2),
        edge(node3, EdgeType.THEN, sId2, sId3),
        edge(node4, EdgeType.THEN, sId3),
      ),
      outgoing = Map(
        edge(node2, EdgeType.LINK, sId1),
        edge(node3, EdgeType.LINK, sId3),
        edge(node4, EdgeType.THEN, sId1),
        edge(node1, EdgeType.THEN, sId2),
        edge(node5, EdgeType.THEN, sId3),
      ),
    )

    val emptyLookup: Lookup = Lookup(Map.empty, Map.empty)

    val absNode1: Node.Abs = makeAbsNodeStub(MnId.Abs(6L))
    val absNode2: Node.Abs = makeAbsNodeStub(MnId.Abs(7L))
    val absNode3: Node.Abs = makeAbsNodeStub(MnId.Abs(8L))

    // Outgoing LINK edges lead only to abstract nodes, THEN and incoming edges have to be ignored.
    val absLookup: Lookup = Lookup(
      incoming = Map(edge(node1, EdgeType.LINK, sId1)),
      outgoing = Map(
        edge(absNode1, EdgeType.LINK, sId1),
        edge(absNode2, EdgeType.LINK, sId2),
        edge(absNode3, EdgeType.THEN, sId3),
        edge(node1, EdgeType.THEN, sId1),
      ),
    )

  "Lookup node maps" should:
    "map neighbor mnId to node, split by edge direction and type" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        lookup.inLink mustBe Map(node1.mnId -> node1, node2.mnId -> node2)
        lookup.outLink mustBe Map(node2.mnId -> node2, node3.mnId -> node3)
        lookup.inThen mustBe Map(node3.mnId -> node3, node4.mnId -> node4)
        lookup.outThen mustBe Map(node4.mnId -> node4, node1.mnId -> node1, node5.mnId -> node5)

    "be empty for empty lookup" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        emptyLookup.inLink mustBe Map.empty
        emptyLookup.outLink mustBe Map.empty
        emptyLookup.inThen mustBe Map.empty
        emptyLookup.outThen mustBe Map.empty

  "Lookup.allOutAbsLink" should:
    "return outgoing LINK neighbors typed as abstract nodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      absLookup.allOutAbsLink[IO].logValue(tn).asserting(_ mustBe Set(absNode1, absNode2))

    "return empty set for empty lookup" in newCase[CaseData]: (tn, data) =>
      import data.*
      emptyLookup.allOutAbsLink[IO].logValue(tn).asserting(_ mustBe Set.empty)

    "fail if some outgoing LINK neighbor is not an abstract node" in newCase[CaseData]: (tn, data) =>
      import data.*
      lookup.allOutAbsLink[IO].logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Expected Node.Abs"))

  "Lookup sample ID maps" should:
    "group neighbor nodes by sample ID, split by edge direction and type" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        lookup.inLinkSampleId mustBe Map(sId1 -> Set(node1), sId2 -> Set(node1, node2))
        lookup.outLinkSampleId mustBe Map(sId1 -> Set(node2), sId3 -> Set(node3))
        lookup.inThenSampleId mustBe Map(sId2 -> Set(node3), sId3 -> Set(node3, node4))
        lookup.outThenSampleId mustBe Map(sId1 -> Set(node4), sId2 -> Set(node1), sId3 -> Set(node5))

    "be empty for empty lookup" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        emptyLookup.inLinkSampleId mustBe Map.empty
        emptyLookup.outLinkSampleId mustBe Map.empty
        emptyLookup.inThenSampleId mustBe Map.empty
        emptyLookup.outThenSampleId mustBe Map.empty

  "Lookup.findInLink(...)" should:
    "return union of nodes of incoming LINK edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findInLink(allSIds)).logValue(tn).asserting(_ mustBe Set(node1, node2))

    "return only nodes of the requested samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findInLink(Set(sId1))).logValue(tn).asserting(_ mustBe Set(node1))

  "Lookup.findOutAbsLink(...)" should:
    "return union of abstract nodes of outgoing LINK edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      absLookup.findOutAbsLink[IO](allSIds).logValue(tn).asserting(_ mustBe Set(absNode1, absNode2))

    "return only nodes of the requested samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      absLookup.findOutAbsLink[IO](Set(sId2)).logValue(tn).asserting(_ mustBe Set(absNode2))

    "fail if some active outgoing LINK neighbor is not an abstract node" in newCase[CaseData]: (tn, data) =>
      import data.*
      lookup.findOutAbsLink[IO](Set(sId3)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Expected Node.Abs"))

  "Lookup.findInThen(...)" should:
    "return union of nodes of incoming THEN edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findInThen(allSIds)).logValue(tn).asserting(_ mustBe Set(node3, node4))

    "return only nodes of the requested samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findInThen(Set(sId2))).logValue(tn).asserting(_ mustBe Set(node3))

  "Lookup.findOutThen(...)" should:
    "return union of nodes of outgoing THEN edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findOutThen(allSIds)).logValue(tn).asserting(_ mustBe Set(node1, node4, node5))

    "return only nodes of the requested samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findOutThen(Set(sId1, sId2))).logValue(tn).asserting(_ mustBe Set(node1, node4))

  "Lookup.find*(...)" should:
    "return empty set for unknown or empty sample IDs" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        lookup.findInLink(Set(unknownSId)) mustBe Set.empty
        lookup.findOutAbsLink[IO](Set(unknownSId)).await mustBe Set.empty
        lookup.findInThen(Set.empty) mustBe Set.empty
        lookup.findOutThen(Set.empty) mustBe Set.empty

    "return empty set for empty lookup" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        emptyLookup.findInLink(allSIds) mustBe Set.empty
        emptyLookup.findOutAbsLink[IO](allSIds).await mustBe Set.empty
        emptyLookup.findInThen(allSIds) mustBe Set.empty
        emptyLookup.findOutThen(allSIds) mustBe Set.empty
