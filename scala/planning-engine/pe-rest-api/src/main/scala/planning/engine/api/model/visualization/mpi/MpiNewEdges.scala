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
| created: 2026-10-07 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import cats.syntax.ext.MT
import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.HnId

final case class MpiNewEdges(
    edges: Set[(HnId, HnId, EdgeType)],
) extends VisualizationMsg

object MpiNewEdges:
  def formMeKeys[F[_]: MT](keys: Set[MeKey]): F[MpiNewEdges] =
    MT(new MpiNewEdges(keys.map(key => (key.src.asHnId, key.trg.asHnId, key.asEdgeType))))
