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
| created: 13-Aug-26 |||||||||||*/

package planning.engine.planner.mpi.actors.manager

import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.visualizer.FakeVisualizer
import planning.engine.planner.mpi.test.data.MapNodeTestData

trait WithTestManager extends MapNodeTestData:
  self: UnitSpecWithIOAndTestKit =>

  trait WithManager extends WithMapNode:
    lazy val fakeVisualizer: FakeVisualizer = FakeVisualizer()

    lazy val manager: TestManager = TestManager("manager-without-nodes", fakeVisualizer)

    lazy val managerOneConNode: TestManager = TestManager("manager-one-con-node", fakeVisualizer)
      .withNode(conNodeData)

    lazy val managerTwoNode: TestManager = TestManager("manager-two-node", fakeVisualizer)
      .withNodes(conNodeData, absNodeData)
