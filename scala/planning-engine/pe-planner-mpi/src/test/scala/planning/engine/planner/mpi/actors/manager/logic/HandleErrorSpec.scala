///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 03.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import planning.engine.planner.mpi.actors.UnitSpecWithTestKit
import Actor.AddNodes
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.test.actors.ManagerTestActor

class HandleErrorSpec extends UnitSpecWithTestKit with ManagerTestActor:
  private class CaseData extends Case with WithManagerActor
  
  "ManagerActor.doHandleNodeError" should:
    "terminate the manager actor after a NodeActorError is received" in newCase[CaseData]: (log, data) =>
      import data.*

      val manager = managerActorEmpty.manager
      val err = new RuntimeException("Node actor boom")
      
      val nodeErrorMsg = Actor.NodeActorError(
        nodeProbe.ref, 
        Some(AddNodes(NodeData(conNodeData), adaptorProbe.ref)), 
        err
      )

      log.msg(manager ! nodeErrorMsg)

      adaptorProbe.expectTerminated(manager)
      succeed

  "ManagerActor.doHandleManagerError" should:
    "terminate the manager actor after receive raises an error" in newCase[CaseData]: (log, data) =>
      import data.*

      val manager = managerActorEmpty.manager
      addNodes(NodeData(conNodeData, conNodeData), manager)

      log.msg(manager ! Actor.UpsertNodesByName(NodeData(conNodeData), adaptorProbe.ref))

      adaptorProbe.expectTerminated(manager)
      succeed
