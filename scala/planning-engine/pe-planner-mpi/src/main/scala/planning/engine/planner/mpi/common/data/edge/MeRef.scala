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

package planning.engine.planner.mpi.common.data.edge

import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.node.Node

final case class MeRef(key: MeKey, src: Node, trg: Node):
  override lazy val toString: String = s"MeRef(${key.toString} | ${src.name} --> ${trg.name})"
