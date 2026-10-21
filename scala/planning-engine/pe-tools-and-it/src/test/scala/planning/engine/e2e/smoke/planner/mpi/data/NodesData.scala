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
| created: 2026-10-21 |||||||||||*/

package planning.engine.e2e.smoke.planner.mpi.data

import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description
import planning.engine.planner.mpi.model.data.node.NodeData

trait NodesData extends MapData:
  // Added separately via `MapMpi.addNode(...)`
  lazy val absSingle: NodeData.Abs = NodeData.Abs(Some(HnName("abs-single")), Description.some("Standalone node"))

  // Added via `MapMpi.upsertNodesByName(...)` and then reused by name in samples
  lazy val conPosition3: NodeData.Con = NodeData.Con(Some(HnName("con-position-3")), None, inPosition.name, IoIndex(3L))

  // Used in samples
  lazy val conObstacleTrue: NodeData.Con =
    NodeData.Con(Some(HnName("con-obstacle-true")), None, inObstacle.name, IoIndex(1L))

  lazy val conSpeedHalf: NodeData.Con =
    NodeData.Con(Some(HnName("con-speed-half")), None, outSpeed.name, IoIndex(5000L))

  lazy val conTurnLeft: NodeData.Con = NodeData.Con(Some(HnName("con-turn-left")), None, outTurn.name, IoIndex(0L))
  lazy val conStep2: NodeData.Con = NodeData.Con(Some(HnName("con-step-2")), None, outStep.name, IoIndex(2L))
  lazy val absAvoid: NodeData.Abs = NodeData.Abs(Some(HnName("abs-avoid")), Description.some("Avoid obstacle"))
  lazy val absMove: NodeData.Abs = NodeData.Abs(Some(HnName("abs-move")), Description.some("Move forward"))
