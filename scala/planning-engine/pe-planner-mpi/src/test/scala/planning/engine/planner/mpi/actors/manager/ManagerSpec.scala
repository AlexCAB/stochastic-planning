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

package planning.engine.planner.mpi.actors.manager

import cats.effect.IO
import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.adaptor.Adaptor
import planning.engine.planner.mpi.model.data.MapNodeTestData

class ManagerSpec extends UnitSpecWithTestKit:
  private class CaseData extends Case with MapNodeTestData:
    val visualizerProbe: TestProbe[Visualizer.Msg] = testKit.createTestProbe[Visualizer.Msg]()
    val adaptorProbe: TestProbe[Adaptor.Msg] = testKit.createTestProbe[Adaptor.Msg]()
    
    lazy val mapManagerActor: Manager.Ref = Manager
      .spawn[IO](Manager.Definition(visualizerProbe.ref), (bh, n) => testKit.spawn(bh, n))
      .unsafeRunSync()

  "MapManagerActor" should:
    "add new node" in newCase[CaseData]: (log, data) =>
      data.mapManagerActor ! Manager.AddNode(data.conNodeData, data.adaptorProbe.ref)

      val conRes = log.msg(data.adaptorProbe.expectMessageType[Adaptor.NodeAdded])
      conRes.id mustBe MnId.Con(1L)
      conRes.name mustBe data.conNodeData.name

      data.mapManagerActor ! Manager.AddNode(data.absNodeData, data.adaptorProbe.ref)

      val absRes = log.msg(data.adaptorProbe.expectMessageType[Adaptor.NodeAdded])
      absRes.id mustBe MnId.Abs(2L)
      absRes.name mustBe data.absNodeData.name
