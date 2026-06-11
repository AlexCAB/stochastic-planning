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

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.adaptors.PpiMapAdaptor.{NodeAdded, Reply}
import planning.engine.planner.mpi.data.MapNodeTestData

class MapManagerActorSpec extends UnitSpecWithTestKit:
  import MapManagerActor._

  private class CaseData extends Case with MapNodeTestData:
    lazy val mapManagerActor: ActorRef[Command] = testKit.spawn(MapManagerActor(), "map-manager")

  "MapManagerActor" should:
    "add new node" in newCase[CaseData]: (log, data) =>
      val probe = testKit.createTestProbe[Reply]()

      data.mapManagerActor ! AddNode(data.conNodeData, probe.ref)

      val conRes = log.msg(probe.expectMessageType[NodeAdded])
      conRes.id mustBe MnId.Con(1L)
      conRes.name mustBe data.conNodeData.name

      data.mapManagerActor ! AddNode(data.absNodeData, probe.ref)

      val absRes = log.msg(probe.expectMessageType[NodeAdded])
      absRes.id mustBe MnId.Abs(2L)
      absRes.name mustBe data.absNodeData.name
