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
| created: 03.09.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.planner.data

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.test.data.MapNodeTestData

class PlannerStateSpec extends UnitSpecWithData with MapNodeTestData:
  private class CaseData extends Case with WithMapNode:
    val name1: IoName = IoName("boolInputNode")
    val name2: IoName = IoName("intInputNode")
    
    val node1: Node.Con = makeConNodeStub(MnId.Con(1L), name1, IoIndex(0))
    val node2: Node.Con = makeConNodeStub(MnId.Con(2L), name1, IoIndex(1))
    val node3: Node.Con = makeConNodeStub(MnId.Con(3L), name2, IoIndex(0))

  "State.init" should:
    "have empty inputNodes and outputNodes maps" in newCase[CaseData]: (_, _) =>
      async[IO]:
        State.init.inputNodes mustBe Map.empty
        State.init.outputNodes mustBe Map.empty

  "State.withNewInNodes(...)" should:
    "add a node under its IoName and IoIndex when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      State.init.withNewInNodes[IO](Set(node1)).asserting:
        _.inputNodes mustBe Map(name1 -> Map(IoIndex(0) -> Set(node1)))

    "add nodes under different IoIndex within the same IoName" in newCase[CaseData]: (_, data) =>
      import data.*
      State.init.withNewInNodes[IO](Set(node1, node2)).asserting:
        _.inputNodes mustBe Map(name1 -> Map(IoIndex(0) -> Set(node1), IoIndex(1) -> Set(node2)))

    "union nodes added at the same IoName and IoIndex across calls" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val dup = makeConNodeStub(MnId.Con(9L), name1, IoIndex(0))
        val state1 = State.init.withNewInNodes[IO](Set(node1)).await
        val state2 = state1.withNewInNodes[IO](Set(dup)).await

        state2.inputNodes mustBe Map(name1 -> Map(IoIndex(0) -> Set(node1, dup)))

    "keep existing input nodes when adding new ones under a different IoName" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state1 = State.init.withNewInNodes[IO](Set(node1)).await
        val state2 = state1.withNewInNodes[IO](Set(node3)).await

        state2.inputNodes mustBe Map(
          name1 -> Map(IoIndex(0) -> Set(node1)),
          name2 -> Map(IoIndex(0) -> Set(node3)),
        )

  "State.withNewOutNodes(...)" should:
    "register a node under its mnId" in newCase[CaseData]: (_, data) =>
      import data.*
      State.init.withNewOutNodes[IO](Set(node1)).asserting:
        _.outputNodes mustBe Map(node1.mnId -> node1)

    "keep existing output nodes when adding new ones" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state1 = State.init.withNewOutNodes[IO](Set(node1)).await
        val state2 = state1.withNewOutNodes[IO](Set(node2)).await

        state2.outputNodes mustBe Map(node1.mnId -> node1, node2.mnId -> node2)

    "replace the entry when the same mnId is added again" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val updated = makeConNodeStub(node1.mnId, name1, IoIndex(5))
        val state1 = State.init.withNewOutNodes[IO](Set(node1)).await
        val state2 = state1.withNewOutNodes[IO](Set(updated)).await

        state2.outputNodes mustBe Map(node1.mnId -> updated)
