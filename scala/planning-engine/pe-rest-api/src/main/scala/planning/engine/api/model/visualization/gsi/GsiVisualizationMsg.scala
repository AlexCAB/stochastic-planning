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
| created: 2025-12-29 |||||||||||*/

package planning.engine.api.model.visualization.gsi

import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnId
import planning.engine.planner.gsi.map.state.{MapGraphState, MapInfoState}

final case class GsiVisualizationMsg(
    inNodes: Set[IoName],
    outNodes: Set[IoName],
    ioValues: Set[(IoName, Set[HnId])],
    concreteNodes: Set[HnId],
    abstractNodes: Set[HnId],
    edgesMapping: Set[(HnId, Set[HnId])],
) extends VisualizationMsg

object GsiVisualizationMsg:
  def fromState[F[_]](info: MapInfoState[F], state: MapGraphState[F]): GsiVisualizationMsg = GsiVisualizationMsg(
    inNodes = info.inNodes.keySet,
    outNodes = info.outNodes.keySet,
    ioValues = state.ioValues.valueMap.toSet.map((k, v) => (k.name, v.map(_.asHnId))),
    concreteNodes = state.graph.nodes.keySet.filter(_.isCon).map(_.asHnId),
    abstractNodes = state.graph.nodes.keySet.filter(_.isAbs).map(_.asHnId),
    edgesMapping = state.graph.structure.srcMap.toSet.map((s, ts) => (s.asHnId, ts.map(_.id.asHnId))),
  )
