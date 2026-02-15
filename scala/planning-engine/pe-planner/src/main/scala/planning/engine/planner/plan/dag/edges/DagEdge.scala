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
| created: 2026-01-04 |||||||||||*/

package planning.engine.planner.plan.dag.edges

import cats.MonadThrow
import planning.engine.common.values.node.SnId
import planning.engine.planner.map.dcg.edges.DcgEdge

final case class DagEdge[F[_]: MonadThrow](
    searchSnId: SnId,
    targetSnId: SnId,
    dcgNode: DcgEdge[F]
):
  override lazy val toString: String =
    s"""DagEdge(
       |searchSnId = ${searchSnId.value}, 
       |targetSnId = ${targetSnId.value}, 
       |dcgNode.key = ${dcgNode})
       |)""".stripMargin
