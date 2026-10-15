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
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.Step.{Active, Next}
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.test.data.NodeTestData

class ActivePathSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val leafMnId: MnId.Con = MnId.Con(1L)
    val leaf: Node = makeNodeStub(leafMnId)

    val time: IoTime = IoTime(1L)
    val key1: StepKey = StepKey.init(leafMnId, 1L)
    val key2: StepKey = StepKey.init(leafMnId, 2L)

    val step1: StepNode = StepNode(key1, Active.init(time))
    val step2: StepNode = StepNode(key2, Active.init(time))

    val pu: PU = PU(p = 0.5, u = 2.0)
    val weight: HuangWeight = new HuangWeight(42L)

  "ActivePath.init(...)" should:
    "start path from the leaf node with its step keys, PU and weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val path = IO.pure(ActivePath.init(leaf, Set(step1, step2), pu, weight)).logValue(tn).await

        path.linkPath mustBe List(leafMnId)
        path.activeNodes mustBe Map(leaf -> Set(key1, key2))
        path.pu mustBe pu
        path.weight mustBe weight

    "map leaf to empty step key set if no steps given" in newCase[CaseData]: (tn, data) =>
      import data.*
      IO.pure(ActivePath.init(leaf, Set.empty, pu, HuangWeight.init)).logValue(tn)
        .asserting(_ mustBe ActivePath(List(leafMnId), Map(leaf -> Set.empty), pu, HuangWeight.init))

    "collapse steps with the same key into a single step key" in newCase[CaseData]: (tn, data) =>
      import data.*
      val sameKeyStep = StepNode(key1, Next.init(time))
      IO.pure(ActivePath.init(leaf, Set(step1, sameKeyStep), pu, weight)).logValue(tn)
        .asserting(_.activeNodes mustBe Map(leaf -> Set(key1)))
