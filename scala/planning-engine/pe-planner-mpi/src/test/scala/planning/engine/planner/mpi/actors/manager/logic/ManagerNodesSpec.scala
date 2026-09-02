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
| created: 09.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.effect.IO
import cats.effect.cps.*
import org.scalatest.Assertion
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.manager.{TestManager, WithTestManager}

import scala.concurrent.duration.*

class ManagerNodesSpec extends UnitSpecWithIOAndTestKit with WithTestManager:
  private class CaseData extends Case with WithManager:
    def checkManagerState(manager: TestManager, expNodes: Map[MnId, Option[HnName]], expNextId: Long): Assertion =
      val state = manager.state

      state.nodeRefMap.keySet mustBe expNodes.keySet
      expNodes.foreach((id, name) => state.nodeRefMap(id).name mustBe name)

      state.nodeNameMap mustBe expNodes.toList
        .collect { case (id, Some(name)) => name -> id }
        .groupBy(_._1).map((name, ids) => name -> ids.map(_._2).toSet)

      state.nextMnId mustBe expNextId

  "Manager.addNode(...)" should:
    "add new node" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val conId1 = manager.api.addNode[IO](conNodeData).logValue(tn).await
        conId1 mustBe conMnId

        fakeVisualizer.expectShowNodesAdded mustBe Map(conId1 -> conNodeData.name)
        fakeVisualizer.probe.expectNoMessage(200.millis)
        fakePlanner.expectConNodeAdded.map(_.mnId) mustBe Set(conId1)
        fakePlanner.probe.expectNoMessage(200.millis)

        checkManagerState(manager, expNodes = Map(conId1 -> conNodeData.name), expNextId = 2L)

        val absId = manager.api.addNode[IO](absNodeData).logValue(tn).await
        absId mustBe absMnId

        fakeVisualizer.expectShowNodesAdded mustBe Map(absId -> absNodeData.name)
        fakeVisualizer.probe.expectNoMessage(200.millis)
        fakePlanner.probe.expectNoMessage(200.millis)

        checkManagerState(
          manager,
          expNodes = Map(conId1 -> conNodeData.name, absId -> absNodeData.name),
          expNextId = 3L,
        )

        val conId2 = manager.api.addNode[IO](conNodeData).logValue(tn).await
        conId2 mustBe MnId.Con(3L)

        fakeVisualizer.expectShowNodesAdded mustBe Map(conId2 -> conNodeData.name)
        fakeVisualizer.probe.expectNoMessage(200.millis)

        fakePlanner.expectConNodeAdded.map(_.mnId) must contain(conId2)
        fakePlanner.probe.expectNoMessage(200.millis)

        checkManagerState(
          manager,
          expNodes = Map(conId1 -> conNodeData.name, absId -> absNodeData.name, conId2 -> conNodeData.name),
          expNextId = 4L,
        )

  "Manager.upsertNodesByName(...)" should:
    "create a new node when upserting an unknown name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val gotId = managerOneConNode.api.upsertNodesByName[IO](absNodeData).logValue(tn).await
        fakeVisualizer.expectShowNodesAdded mustBe Map(gotId -> absNodeData.name)
        fakePlanner.probe.expectNoMessage(200.millis)
        gotId mustBe absMnId

        checkManagerState(
          managerOneConNode,
          expNodes = managerOneConNode.nodes.view.mapValues(_.name).toMap + (gotId -> absNodeData.name),
          expNextId = 3L,
        )

    "return the existing node's MnId when upserting an already-known name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val gotId = managerOneConNode.api.upsertNodesByName[IO](conNodeData).logValue(tn).await
        fakeVisualizer.probe.expectNoMessage(500.millis) // No new node created, so no visualizer notification
        fakePlanner.probe.expectNoMessage(200.millis)
        gotId mustBe managerOneConNode.srcMnId

        checkManagerState(
          managerOneConNode,
          expNodes = managerOneConNode.nodes.view.mapValues(_.name).toMap,
          expNextId = 2L,
        )

    "terminate when UpsertNodesByName finds multiple IDs for the same name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        manager.withNodes(conNodeData, conNodeData)

        manager.api.upsertNodesByName[IO](conNodeData).logValue(tn).attempt.await
        fakeVisualizer.probe.expectTerminated(manager.ref)
        succeed
