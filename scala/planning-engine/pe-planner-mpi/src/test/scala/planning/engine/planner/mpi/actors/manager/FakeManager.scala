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
| created: 11.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.FakeActorBase
import planning.engine.planner.mpi.actors.manager.data.Message.NodeActorError
import planning.engine.planner.mpi.actors.manager.data.State
import planning.engine.planner.mpi.actors.manager.logic.ApiImpl
import planning.engine.planner.mpi.actors.node.Node

final case class FakeManager(api: Manager, probe: TestProbe[Manager.Msg]) extends FakeActorBase:
  def getState(using testKit: ActorTestKit): FakeManager.ManagerState =
    Tuple.fromProductTyped(getActorState[State])

  def expectReportedError: (Node, Throwable) =
    val msg = probe.expectMessageType[NodeActorError]
    (msg.nodeRef, msg.err)

object FakeManager:
  type ManagerState = (Map[MnId, Node], Map[HnName, Set[MnId]], Long)

  def apply()(using testKit: ActorTestKit): FakeManager =
    val probe = testKit.createTestProbe[Manager.Msg]("FakeManagerProbe")
    FakeManager(ApiImpl(probe.ref), probe)
