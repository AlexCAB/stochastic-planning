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
| created: 2026-10-05 |||||||||||*/



package planning.engine.api.model.map.extensions.mpi

import planning.engine.api.model.map.payload.HiddenNodeDef
import planning.engine.planner.mpi.model.data.node.NodeData

object HiddenNodeDefEx:
  extension (hiddenNodeDef: HiddenNodeDef)
    def toNodeData: NodeData = ???
     