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

package planning.engine.planner.mpi.actors.planner.data.state

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.test.data.NodeTestData

class StructSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val name1: IoName = IoName("boolInputNode")
    val name2: IoName = IoName("intInputNode")

    val node1: Node.Con = makeConNodeStub(MnId.Con(1L), name1, IoIndex(0))
    val node2: Node.Con = makeConNodeStub(MnId.Con(2L), name1, IoIndex(1))
    val node3: Node.Con = makeConNodeStub(MnId.Con(3L), name2, IoIndex(0))

    lazy val struct: Struct = Struct.init.withNewInNodes(Set(node1, node2, node3))

  "Struct.init" should:
    "have empty inputNodes and outputNodes maps" in newCase[CaseData]: (_, _) =>
      async[IO]:
        Struct.init.inputNodes mustBe Map.empty
        Struct.init.outputNodes mustBe Map.empty

  "Struct.withNewInNodes(...)" should:
    "add a node under its IoValue when empty" in newCase[CaseData]: (_, data) =>
      import data.*
      Struct.init.withNewInNodes(Set(node1)).pure[IO]
        .asserting(_.inputNodes mustBe Map(node1.ioValue -> node1))

    "add nodes under different IoIndex within the same IoName" in newCase[CaseData]: (_, data) =>
      import data.*
      Struct.init.withNewInNodes(Set(node1, node2)).pure[IO]
        .asserting(_.inputNodes mustBe Map(node1.ioValue -> node1, node2.ioValue -> node2))

    "replace the entry when a node with the same IoValue is added again" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val dup = makeConNodeStub(MnId.Con(9L), name1, IoIndex(0))
        val state1 = Struct.init.withNewInNodes(Set(node1))
        val state2 = state1.withNewInNodes(Set(dup))

        state2.inputNodes mustBe Map(IoValue(name1, IoIndex(0)) -> dup)

    "keep existing input nodes when adding new ones under a different IoName" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state1 = Struct.init.withNewInNodes(Set(node1))
        val state2 = state1.withNewInNodes(Set(node3))

        state2.inputNodes mustBe Map(node1.ioValue -> node1, node3.ioValue -> node3)

  "Struct.withNewOutNodes(...)" should:
    "register a node under its mnId" in newCase[CaseData]: (_, data) =>
      import data.*
      Struct.init.withNewOutNodes(Set(node1)).pure[IO]
        .asserting(_.outputNodes mustBe Map(node1.mnId -> node1))

    "keep existing output nodes when adding new ones" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val state1 = Struct.init.withNewOutNodes(Set(node1))
        val state2 = state1.withNewOutNodes(Set(node2))

        state2.outputNodes mustBe Map(node1.mnId -> node1, node2.mnId -> node2)

    "replace the entry when the same mnId is added again" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val updated = makeConNodeStub(node1.mnId, name1, IoIndex(5))
        val state1 = Struct.init.withNewOutNodes(Set(node1))
        val state2 = state1.withNewOutNodes(Set(updated))

        state2.outputNodes mustBe Map(node1.mnId -> updated)

  "Struct.findInNodes(...)" should:
    "return nodes for all requested IoValues" in newCase[CaseData]: (_, data) =>
      import data.*
      struct.findInNodes[IO](Set(node1.ioValue, node3.ioValue))
        .asserting(_ mustBe Set(node1, node3))

    "fail when some requested IoValue is not known" in newCase[CaseData]: (_, data) =>
      import data.*
      struct.findInNodes[IO](Set(node1.ioValue, IoValue(name2, IoIndex(7))))
        .assertThrowsError[AssertionError](_.getMessage must include("Struct.findInNodes: missing input nodes"))
