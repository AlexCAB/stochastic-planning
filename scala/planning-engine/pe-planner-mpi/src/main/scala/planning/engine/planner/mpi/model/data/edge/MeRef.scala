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
| created: 04.07.2026 |||||||||||*/

package planning.engine.planner.mpi.model.data.edge

import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.node.Node

final case class MeRef(key: MeKey, srcNode: Node, trgNode: Node):
  override lazy val toString: String = s"MeRef(${key.toString} | ${srcNode.name} --> ${trgNode.name})"
