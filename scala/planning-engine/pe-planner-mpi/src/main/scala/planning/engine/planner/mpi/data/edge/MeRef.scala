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

package planning.engine.planner.mpi.data.edge

import planning.engine.common.graph.edges.MeKey
import planning.engine.planner.mpi.actors.node.NodeActor

final case class MeRef(key: MeKey, src: NodeActor.Ref, trg: NodeActor.Ref):
  override lazy val toString: String = s"MeRef(${key.toString} | ${src.path.name} --> ${trg.path.name})"
