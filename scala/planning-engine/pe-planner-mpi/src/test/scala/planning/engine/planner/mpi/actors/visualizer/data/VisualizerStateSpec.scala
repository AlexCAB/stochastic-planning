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
| created: 05.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.data

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit

class VisualizerStateSpec extends UnitSpecWithIOAndTestKit:
  private class CaseData extends Case:
    val conId1: MnId.Con = MnId.Con(1L)
    val conId2: MnId.Con = MnId.Con(3L)
    val absId1: MnId.Abs = MnId.Abs(2L)

    val conName: Option[HnName] = Some(HnName("Con Node"))
    val absName: Option[HnName] = Some(HnName("Abs Node"))

    val linkKey: Link = Link(conId1, absId1)
    val thenKey: Then = Then(absId1, conId1)

    lazy val stateWithNodes: State = State.init
      .withNodesAdded(Map(conId1 -> conName, absId1 -> absName))

    lazy val stateWithEdges: State = State.init
      .withEdgesAdded(Set(linkKey, thenKey))

  "State.withNodesAdded(...)" should:
    "split added node IDs into conNodes and absNodes" in newCase[CaseData]: (_, data) =>
      import data.*
      val state = State.init.withNodesAdded(Map(conId1 -> conName, absId1 -> absName))

      async[IO]:
        state.conNodes mustBe Map(conId1 -> conName)
        state.absNodes mustBe Map(absId1 -> absName)

    "add to existing nodes without removing them" in newCase[CaseData]: (_, data) =>
      import data.*
      val state = stateWithNodes.withNodesAdded(Map(conId2 -> None))

      async[IO]:
        state.conNodes mustBe Map(conId1 -> conName, conId2 -> None)
        state.absNodes mustBe Map(absId1 -> absName)

    "replace a Con node's name when the ID already exists" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val newName = Some(HnName("Updated Con Node"))
        val state = stateWithNodes.withNodesAdded(Map(conId1 -> newName))

        state.conNodes mustBe Map(conId1 -> newName)

    "replace an Abs node's name when the ID already exists" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val newName = Some(HnName("Updated Abs Node"))
        val state = stateWithNodes.withNodesAdded(Map(absId1 -> newName))

        state.absNodes mustBe Map(absId1 -> newName)

  "State.withEdgesAdded(...)" should:
    "add Link edge ends to srcLinkMap and trgLinkMap" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state = State.init.withEdgesAdded(Set(linkKey))

        state.srcLinkMap mustBe Map(conId1 -> Set(linkKey.trgEnd))
        state.trgLinkMap mustBe Map(absId1 -> Set(linkKey.srcEnd))
        state.srcThenMap mustBe Map.empty
        state.trgThenMap mustBe Map.empty

    "add Then edge ends to srcThenMap and trgThenMap" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state = State.init.withEdgesAdded(Set(thenKey))

        state.srcThenMap mustBe Map(absId1 -> Set(thenKey.trgEnd))
        state.trgThenMap mustBe Map(conId1 -> Set(thenKey.srcEnd))
        state.srcLinkMap mustBe Map.empty
        state.trgLinkMap mustBe Map.empty

    "add to existing edges without removing them" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val otherLink = Link(absId1, conId2)
        val state = stateWithEdges.withEdgesAdded(Set(otherLink))

        state.srcLinkMap mustBe Map(conId1 -> Set(linkKey.trgEnd), absId1 -> Set(otherLink.trgEnd))
        state.trgLinkMap mustBe Map(absId1 -> Set(linkKey.srcEnd), conId2 -> Set(otherLink.srcEnd))

    "not duplicate an edge end when the same edge key is added again" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state = stateWithEdges.withEdgesAdded(Set(linkKey))

        state.srcLinkMap mustBe Map(conId1 -> Set(linkKey.trgEnd))
        state.trgLinkMap mustBe Map(absId1 -> Set(linkKey.srcEnd))
