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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.common.data.node

import planning.engine.common.values.node.MnId

enum NodeType:
  case Concrete, Abstract

  def toMnId(rawId: Long): MnId = this match
    case NodeType.Concrete => MnId.Con(rawId)
    case NodeType.Abstract => MnId.Abs(rawId)
