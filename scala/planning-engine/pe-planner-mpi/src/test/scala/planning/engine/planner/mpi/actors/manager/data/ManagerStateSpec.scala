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
import planning.engine.planner.mpi.actors.manager.logic.Actor
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.actors.StaticTestActors
import planning.engine.planner.mpi.test.data.MapNodeTestData

class ManagerStateSpec extends UnitSpecWithIOAndTestKit with StaticTestActors:
  private class CaseData extends Case with MapNodeTestData with WithStaticActors:
    def spawnRefs(definitions: List[NodeActor.Def]): Map[NodeActor.Ref, NodeActor.Def] =
      definitions.map(d => testKit.createTestProbe[NodeActor.Msg]().ref -> d).toMap

    lazy val conMnId: MnId.Con = MnId.Con(1L)
    lazy val absMnId: MnId.Abs = MnId.Abs(2L)

    lazy val stateWithConNode: Actor.State = Actor.State.init
      .withNewNodes[IO](NodeData(conNodeData), staticActors, spawnRefs)
      .unsafeRunSync()._2

    lazy val stateWithNodes: Actor.State = Actor.State.init
      .withNewNodes[IO](NodeData(conNodeData, absNodeData), staticActors, spawnRefs)
      .unsafeRunSync()._2

  "State.withNewNodes(...)" should:
    "add a named node to nodeRefMap and nodeNameMap, and increment nextId" in newCase[CaseData]: (_, data) =>
      import data.*

      Actor.State.init.withNewNodes[IO](NodeData(conNodeData, absNodeData), staticActors, spawnRefs)
        .asserting:
          case (nodeRefs, state) =>
            state.nodeRefMap.keySet mustBe Set(conMnId, absMnId)
            state.nodeRefMap.values.toSet mustBe nodeRefs.keySet
            state.nextId mustBe 3L

            state.nodeNameMap mustBe Map(
              conNodeData.name.get -> Set(conMnId),
              absNodeData.name.get -> Set(absMnId),
            )

    "add nodes with duplicate data" in newCase[CaseData]: (_, data) =>
      import data.*
      val id1 = MnId.Con(1L)
      val id2 = MnId.Con(2L)

      Actor.State.init.withNewNodes[IO](NodeData(conNodeData, conNodeData), staticActors, spawnRefs)
        .asserting:
          case (_, state) =>
            state.nodeRefMap.keySet mustBe Set(id1, id2)
            state.nextId mustBe 3L
            state.nodeNameMap mustBe Map(conNodeData.name.get -> Set(id1, id2))

    "not add to nodeNameMap for a node without a name" in newCase[CaseData]: (_, data) =>
      import data.*
      val unnamedData = absNodeData.copy(name = None)

      Actor.State.init.withNewNodes[IO](NodeData(unnamedData), staticActors, spawnRefs)
        .asserting { case (_, state) => state.nodeNameMap mustBe Map.empty }

    "raise an error when a node ID already exists in state" in newCase[CaseData]: (_, data) =>
      import data.*
      val conflictingState = Actor.State(
        nodeRefMap = Map(conMnId -> testKit.createTestProbe[NodeActor.Msg]().ref),
        nodeNameMap = Map.empty,
        nextId = 1L, // re-assigning from 1 collides with the existing conMnId entry
      )

      conflictingState.withNewNodes[IO](NodeData(conNodeData), staticActors, spawnRefs)
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
      val incorrectState = Actor.State(
        nodeRefMap = Map.empty,
        nodeNameMap = Map(data.conNodeData.name.get -> Set(MnId.Con(1L), MnId.Con(2L))),
        nextId = 3L,
      )

      incorrectState.findByName[IO](Set(data.conNodeData.name.get))
        .assertThrowsError[AssertionError](_.getMessage must include("Expected exactly one node ID for name"))
