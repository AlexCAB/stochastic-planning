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

package planning.engine.planner.mpi.actors

import cats.effect.IO
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.model.data.MapNodeTestData
import planning.engine.planner.mpi.model.messages.{AdaptorMsg, ManagerMsg}

class MapManagerActorSpec extends UnitSpecWithTestKit:
  import AdaptorMsg._, ManagerMsg._

  private class CaseData extends Case with MapNodeTestData:
    lazy val mapManagerActor: ActorRef[ManagerMsg] = testKit.spawn(MapManagerActor[IO], "map-manager")

  "MapManagerActor" should:
    "add new node" in newCase[CaseData]: (log, data) =>
      val probe = testKit.createTestProbe[NodeResult]()

      data.mapManagerActor ! AddNode(data.conNodeData, probe.ref)

      val conRes = log.msg(probe.expectMessageType[NodeAdded])
      conRes.id mustBe MnId.Con(1L)
      conRes.name mustBe data.conNodeData.name

      data.mapManagerActor ! AddNode(data.absNodeData, probe.ref)

      val absRes = log.msg(probe.expectMessageType[NodeAdded])
      absRes.id mustBe MnId.Abs(2L)
      absRes.name mustBe data.absNodeData.name
