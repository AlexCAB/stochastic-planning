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

import planning.engine.common.values.node.SnId
import planning.engine.planner.map.dcg.edges.DcgEdgeData

final case class DagEdge(
    searchSnId: SnId,
    targetSnId: SnId,
    dcgNode: DcgEdgeData
):
  override def toString: String =
    s"""DagEdge(
       |searchSnId = ${searchSnId.value}, 
       |targetSnId = ${targetSnId.value}, 
       |dcgNode.key = ${dcgNode})
       |)""".stripMargin
