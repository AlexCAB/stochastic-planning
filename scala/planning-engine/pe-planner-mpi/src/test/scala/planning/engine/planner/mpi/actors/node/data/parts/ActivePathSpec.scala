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
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.test.data.NodeTestData

class ActivePathSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val leafMnId: MnId.Con = MnId.Con(1L)
    val leaf: Node = makeNodeStub(leafMnId)

    val parentMnId: MnId.Abs = MnId.Abs(2L)
    val parent: Node = makeNodeStub(parentMnId)

    val key1: StepKey = StepKey.init(leafMnId, 1L)
    val key2: StepKey = StepKey.init(leafMnId, 2L)
    val parentKey: StepKey = StepKey.init(parentMnId, 1L)

    val pu: PU = PU(p = 0.5, u = 2.0)
    val parentPu: PU = PU(p = 0.25, u = 4.0)
    val nextWeight: HuangWeight = new HuangWeight(21L)

    lazy val initPath: ActivePath = ActivePath.init(leaf, Set(key1, key2), pu)

  "ActivePath.init(...)" should:
    "start path from the leaf node with its step keys, PU and initial Huang weight" in newCase[CaseData]:
      (tn, data) =>
        import data.*
        async[IO]:
          val path = IO.pure(initPath).logValue(tn).await

          path.linkPath mustBe List(leafMnId)
          path.activeNodes mustBe Map(leaf -> Set(key1, key2))
          path.pu mustBe pu
          path.weight mustBe HuangWeight.init

    "map leaf to empty step key set if no steps given" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(ActivePath.init(leaf, Set.empty, pu)).logValue(tn)
        .asserting(_ mustBe ActivePath(List(leafMnId), Map(leaf -> Set.empty), pu, HuangWeight.init))

  "ActivePath.append(...)" should:
    "prepend node to link path, add its steps to active nodes and replace PU" in newCase[CaseData]: (tn, data) =>
      import data.*

      val expected = ActivePath(
        linkPath = List(parentMnId, leafMnId),
        activeNodes = Map(leaf -> Set(key1, key2), parent -> Set(parentKey)),
        pu = parentPu,
        weight = HuangWeight.init,
      )

      initPath.append[IO](parent, Set(parentKey), parentPu).logValue(tn).asserting(_ mustBe expected)

    "fail if node already exists in the link path" in newCase[CaseData]: (tn, data) =>
      import data.*
      initPath.append[IO](leaf, Set(key1), parentPu).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Node $leafMnId already exists in the link path"))

  "ActivePath.withNextWeight(...)" should:
    "replace only the weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(initPath.withNextWeight(nextWeight)).logValue(tn)
        .asserting(_ mustBe initPath.copy(weight = nextWeight))
