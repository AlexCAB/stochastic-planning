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
| created: 01.07.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import org.apache.pekko.actor.testkit.typed.scaladsl.TestProbe
import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import planning.engine.planner.mpi.adaptor.manager.ManagerAdaptor
import planning.engine.planner.mpi.test.actors.ManagerTestActor

class ManageEdgesSpec extends UnitSpecWithTestKit with ManagerTestActor:
  private class CaseData extends Case with WithManagerActor:
    val adaptorProbe: TestProbe[ManagerAdaptor.Msg] = testKit.createTestProbe[ManagerAdaptor.Msg]()

  // TODO
