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
| created: 2026-10-16 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.parts

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath.ActiveNode
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.test.data.NodeTestData

class ActivePathSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val leafMnId: MnId.Con = MnId.Con(1L)
    val leaf: Node.Con = makeConNodeStub(leafMnId, inVarName, IoIndex(0))

    val parentMnId: MnId.Abs = MnId.Abs(2L)
    val parent: Node.Abs = makeAbsNodeStub(parentMnId)

    val grandParentMnId: MnId.Abs = MnId.Abs(3L)
    val grandParent: Node.Abs = makeAbsNodeStub(grandParentMnId)

    val key1: StepKey = StepKey.init(leafMnId, 1L)
    val key2: StepKey = StepKey.init(leafMnId, 2L)
    val parentKey: StepKey = StepKey.init(parentMnId, 1L)

    val pu: PU = PU(p = 0.5, u = 2.0)
    val parentPu: PU = PU(p = 0.25, u = 4.0)
    val nextWeight: HuangWeight = new HuangWeight(21L)

    lazy val initPath: ActivePath = ActivePath.init(leaf, Set(key1, key2), pu)

  "ActivePath.init(...)" should:
    "start path from the leaf node with its step keys, PU and initial Huang weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val path = IO.pure(initPath).logValue(tn).await

        path.root mustBe leafMnId
        path.path mustBe List.empty
        path.activeNodes mustBe Map(leafMnId -> ActiveNode(leaf, pu, Set(key1, key2)))
        path.weight mustBe HuangWeight.init

    "map leaf to empty step key set if no steps given" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(ActivePath.init(leaf, Set.empty, pu)).logValue(tn)
        .asserting(_ mustBe ActivePath(
          leafMnId,
          List.empty,
          Map(leafMnId -> ActiveNode(leaf, pu, Set.empty)),
          HuangWeight.init,
        ))

  "ActivePath.append(...)" should:
    "prepend node to link path and add it with its PU and steps to active nodes" in newCase[CaseData]: (tn, data) =>
      import data.*

      val expected = ActivePath(
        root = leafMnId,
        path = List(parentMnId),
        activeNodes = Map(
          leafMnId -> ActiveNode(leaf, pu, Set(key1, key2)),
          parentMnId -> ActiveNode(parent, parentPu, Set(parentKey)),
        ),
        weight = HuangWeight.init,
      )

      initPath.append[IO](parent, Set(parentKey), parentPu).logValue(tn).asserting(_ mustBe expected)

    "fail if node already exists in the link path" in newCase[CaseData]: (tn, data) =>
      import data.*
      initPath.append[IO](parent, Set(parentKey), parentPu)
        .flatMap(_.append[IO](parent, Set(parentKey), parentPu)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(
          s"Node $parentMnId already exists in the link path",
        ))

  "ActivePath.withNextWeight(...)" should:
    "replace only the weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(initPath.withNextWeight(nextWeight)).logValue(tn)
        .asserting(_ mustBe initPath.copy(weight = nextWeight))

  "ActivePath.nodes" should:
    "return active nodes with their PU" in newCase[CaseData]: (tn, data) =>
      import data.*
      initPath.append[IO](parent, Set(parentKey), parentPu).logValue(tn)
        .asserting(_.nodes mustBe Map(leafMnId -> pu, parentMnId -> parentPu))

  "ActivePath.edges" should:
    "be empty for path with only the root" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(initPath.edges).logValue(tn).asserting(_ mustBe Set.empty)

    "contain edge from root to the single appended node" in newCase[CaseData]: (tn, data) =>
      import data.*
      initPath.append[IO](parent, Set(parentKey), parentPu).logValue(tn)
        .asserting(_.edges mustBe Set(leafMnId -> parentMnId))

    "contain edges from root up to the last appended node" in newCase[CaseData]: (tn, data) =>
      import data.*
      initPath.append[IO](parent, Set(parentKey), parentPu)
        .flatMap(_.append[IO](grandParent, Set.empty, parentPu)).logValue(tn)
        .asserting(_.edges mustBe Set(leafMnId -> parentMnId, parentMnId -> grandParentMnId))
