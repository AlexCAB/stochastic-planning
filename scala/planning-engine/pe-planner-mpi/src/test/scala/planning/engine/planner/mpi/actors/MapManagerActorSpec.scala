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
import planning.engine.planner.mpi.adaptors.PpiMapAdaptor.{NodeAdded, Reply}
import planning.engine.planner.mpi.data.MapNodeTestData

class MapManagerActorSpec extends UnitSpecWithTestKit:
  import MapManagerActor._

  private class CaseData extends Case with MapNodeTestData:

    lazy val mapManagerActor: ActorRef[Command] = testKit.spawn(MapManagerActor(0))

  "MapManagerActor" should:
    "add new concrete node" in newCase[CaseData]: (log, data) =>
      val probe = testKit.createTestProbe[Reply]()

      data.mapManagerActor ! AddNode(data.conNodeData, probe.ref)
      log.msg(probe.expectMessageType[NodeAdded]).name mustBe data.conNodeData.name
