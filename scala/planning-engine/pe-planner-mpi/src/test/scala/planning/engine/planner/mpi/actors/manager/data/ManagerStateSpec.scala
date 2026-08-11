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
| created: 30.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.data

import cats.effect.IO
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.{FakeNode, Node}
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.data.MapNodeTestData

class ManagerStateSpec extends UnitSpecWithIOAndTestKit:
  private class CaseData extends Case with MapNodeTestData:
    def spawnNode(rawId: Long, data: NodeData): IO[Node] = 
      IO.delay(FakeNode(data.nodeType.toMnId(rawId), data.name).api)

    lazy val conMnId: MnId.Con = MnId.Con(1L)
    lazy val absMnId: MnId.Abs = MnId.Abs(2L)

    lazy val stateEmpty: State = State.init
    
    lazy val stateWithConNode: State = stateEmpty
      .withNewNodes[IO](NodeData(conNodeData), spawnNode)
      .unsafeRunSync()._2

    lazy val stateWithNodes: State = stateEmpty
      .withNewNodes[IO](NodeData(conNodeData, absNodeData), spawnNode)
      .unsafeRunSync()._2

  "State.withNewNodes(...)" should:
    "add a named node to nodeRefMap and nodeNameMap, and increment nextId" in newCase[CaseData]: (_, data) =>
      import data.*

      stateEmpty.withNewNodes[IO](NodeData(conNodeData, absNodeData), spawnNode)
        .asserting: (nodes, state) =>
          state.nodeRefMap.keySet mustBe Set(conMnId, absMnId)
          state.nodeRefMap.values.toSet mustBe nodes.toSet
          state.nextId mustBe 3L

          state.nodeNameMap mustBe Map(
            conNodeData.name.get -> Set(conMnId),
            absNodeData.name.get -> Set(absMnId),
          )

    "add nodes with duplicate data" in newCase[CaseData]: (_, data) =>
      import data.*
      val id1 = MnId.Con(1L)
      val id2 = MnId.Con(2L)

      stateEmpty.withNewNodes[IO](NodeData(conNodeData, conNodeData), spawnNode)
        .asserting: (_, state) =>
          state.nodeRefMap.keySet mustBe Set(id1, id2)
          state.nextId mustBe 3L
          state.nodeNameMap mustBe Map(conNodeData.name.get -> Set(id1, id2))

    "not add to nodeNameMap for a node without a name" in newCase[CaseData]: (_, data) =>
      import data.*
      val unnamedData = absNodeData.copy(name = None)

      stateEmpty.withNewNodes[IO](NodeData(unnamedData), spawnNode)
        .asserting((_, state) => state.nodeNameMap mustBe Map.empty)

    "raise an error when a node ID already exists in state" in newCase[CaseData]: (_, data) =>
      import data.*
      val conflictingState = stateWithNodes.copy(nextId = 1L) // re-assigning from 1 collides with the existing conMnId entry
        
      conflictingState.withNewNodes[IO](NodeData(conNodeData), spawnNode)
        .assertThrowsError[AssertionError](_.getMessage must include("Node IDs already exist in the current state"))

  "State.findByName(...)" should:
    "return a MnId-to-name map for a found name" in newCase[CaseData]: (_, data) =>
      data.stateWithConNode
        .findByName[IO](Set(data.conNodeData.name.get))
        .asserting(_ mustBe Map(MnId.Con(1L) -> data.conNodeData.name.get))

    "return entries for all found names" in newCase[CaseData]: (_, data) =>
      data.stateWithNodes
        .findByName[IO](Set(data.conNodeData.name.get, data.absNodeData.name.get))
        .asserting(_ mustBe Map(
          MnId.Con(1L) -> data.conNodeData.name.get,
          MnId.Abs(2L) -> data.absNodeData.name.get,
        ))

    "return an empty map when no name matches" in newCase[CaseData]: (_, data) =>
      data.stateWithConNode
        .findByName[IO](Set(data.absNodeData.name.get))
        .asserting(_ mustBe Map.empty)

    "raise an error when a name maps to more than one node ID" in newCase[CaseData]: (_, data) =>
      val duplicateNameState = State(
        nodeRefMap = Map.empty,
        nodeNameMap = Map(data.conNodeData.name.get -> Set(MnId.Con(1L), MnId.Con(2L))),
        nextId = 3L,
      )

      duplicateNameState.findByName[IO](Set(data.conNodeData.name.get))
        .assertThrowsError[AssertionError](_.getMessage must include("Expected exactly one node ID for name"))
