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
| created: 2026-10-20 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import cats.syntax.ext.MT
import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.model.data.visualization.ActiveForest

final case class MpiActiveForest(
    time: IoTime,
    nodes: Map[MnId, PU],
    roots: Set[MnId],
    edges: Set[(MnId, MnId)],
) extends VisualizationMsg

object MpiActiveForest:
  def apply[F[_]: MT](time: IoTime, forest: ActiveForest): F[MpiActiveForest] =
    MT(MpiActiveForest(time, forest.nodes, forest.roots, forest.edges))
