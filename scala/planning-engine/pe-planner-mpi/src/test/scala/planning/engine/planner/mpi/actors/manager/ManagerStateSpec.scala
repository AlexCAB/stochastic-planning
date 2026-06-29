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

package planning.engine.planner.mpi.actors.manager

import cats.effect.IO
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.test.actors.StaticTestActors
import planning.engine.planner.mpi.test.data.MapNodeTestData

class ManagerStateSpec extends UnitSpecWithIOAndTestKit with StaticTestActors:
  private class CaseData extends Case with MapNodeTestData with WithStaticActors:
    val conNodeRef: NodeActor.Ref = testKit.createTestProbe[NodeActor.Msg]().ref
    val absNodeRef: NodeActor.Ref = testKit.createTestProbe[NodeActor.Msg]().ref

    lazy val conDef: NodeActor.ConDef = NodeActor.ConDef(MnId.Con(1L), conNodeData, staticActors)
    lazy val absDef: NodeActor.AbsDef = NodeActor.AbsDef(MnId.Abs(2L), absNodeData, staticActors)

    lazy val stateWithConNode: ManagerActor.State = ManagerActor.State.init
      .withNewNodes[IO](Map(conNodeRef -> conDef))
      .unsafeRunSync()

    lazy val stateWithNodes: ManagerActor.State = ManagerActor.State.init
      .withNewNodes[IO](Map(conNodeRef -> conDef, absNodeRef -> absDef))
      .unsafeRunSync()

  "State.withNewNodes(...)" should:
    "add a named node to nodeRefs and nodeNames, and increment nextId" in newCase[CaseData]: (_, data) =>
      val newNodes = Map(data.conNodeRef -> data.conDef, data.absNodeRef -> data.absDef)

      ManagerActor.State.init.withNewNodes[IO](newNodes).asserting: state =>
        state.nodeRefs mustBe Map(MnId.Con(1L) -> data.conNodeRef, MnId.Abs(2L) -> data.absNodeRef)
        state.nextId mustBe 3L

        state.nodeNames mustBe Map(
          data.conNodeData.name.get -> Set(MnId.Con(1L)),
          data.absNodeData.name.get -> Set(MnId.Abs(2L)),
        )

    "not add to nodeNames for a node without a name" in newCase[CaseData]: (_, data) =>
      val unnamedDef = NodeActor.AbsDef(MnId.Abs(2L), data.absNodeData.copy(name = None), data.staticActors)

      ManagerActor.State.init.withNewNodes[IO](Map(data.absNodeRef -> unnamedDef)).asserting: state =>
        state.nodeNames mustBe Map.empty

    "raise an error when a node ref already exists in state" in newCase[CaseData]: (_, data) =>
      data.stateWithConNode
        .withNewNodes[IO](Map(data.conNodeRef -> data.conDef))
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
      val incorrectState = ManagerActor.State(
        nodeRefs = Map.empty,
        nodeNames = Map(data.conNodeData.name.get -> Set(MnId.Con(1L), MnId.Con(2L))),
        nextId = 3L,
      )

      incorrectState.findByName[IO](Set(data.conNodeData.name.get))
        .assertThrowsError[AssertionError](_.getMessage must include("Expected exactly one node ID for name"))
