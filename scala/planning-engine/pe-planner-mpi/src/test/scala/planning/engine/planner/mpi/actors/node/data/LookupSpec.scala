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

package planning.engine.planner.mpi.actors.node.data

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.test.data.NodeTestData

class LookupSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val sId1: SampleId = SampleId(1L)
    val sId2: SampleId = SampleId(2L)
    val sId3: SampleId = SampleId(3L)
    val unknownSId: SampleId = SampleId(99L)

    val node1: Node = makeNodeStub(MnId.Con(1L))
    val node2: Node = makeNodeStub(MnId.Abs(2L))
    val node3: Node = makeNodeStub(MnId.Abs(3L))
    val node4: Node = makeNodeStub(MnId.Abs(4L))

    // Each map has distinct content, to make sure each find method use the correct map.
    val lookup: Lookup = Lookup(
      inLink = Map(sId1 -> Set(node1), sId2 -> Set(node1, node2)),
      outLink = Map(sId1 -> Set(node2), sId3 -> Set(node3)),
      inThen = Map(sId2 -> Set(node3), sId3 -> Set(node3, node4)),
      outThen = Map(sId1 -> Set(node4), sId2 -> Set(node1), sId3 -> Set(node2)),
    )

    val emptyLookup: Lookup = Lookup(Map.empty, Map.empty, Map.empty, Map.empty)

  "Lookup.findInLinks(...)" should:
    "return union of nodes of incoming LINK edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findInLinks(Set(sId1, sId2, sId3))).logValue(tn).asserting(_ mustBe Set(node1, node2))

  "Lookup.findOutLinks(...)" should:
    "return union of nodes of outgoing LINK edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findOutLinks(Set(sId1, sId2, sId3))).logValue(tn).asserting(_ mustBe Set(node2, node3))

  "Lookup.findInThen(...)" should:
    "return union of nodes of incoming THEN edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findInThen(Set(sId1, sId2, sId3))).logValue(tn).asserting(_ mustBe Set(node3, node4))

  "Lookup.findOutThen(...)" should:
    "return union of nodes of outgoing THEN edges for multiple samples" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(lookup.findOutThen(Set(sId1, sId2, sId3))).logValue(tn).asserting(_ mustBe Set(node1, node2, node4))
