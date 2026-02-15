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

package planning.engine.api.model.visualization

import io.circe.{Encoder, Decoder}
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}

final case class MapVisualizationMsg(
    inNodes: Set[IoName],
    outNodes: Set[IoName],
    ioValues: Set[(IoName, Set[HnId])],
    concreteNodes: Set[HnId],
    abstractNodes: Set[HnId],
    edgesMapping: Set[(HnId, Set[HnId])]
)

object MapVisualizationMsg:
  import io.circe.generic.semiauto.*

  import planning.engine.api.model.json.values.*

  implicit val decoder: Decoder[MapVisualizationMsg] = deriveDecoder[MapVisualizationMsg]
  implicit val encoder: Encoder[MapVisualizationMsg] = deriveEncoder[MapVisualizationMsg]

  def fromState[F[_]](info: MapInfoState[F], state: MapGraphState[F]): MapVisualizationMsg = MapVisualizationMsg(
    inNodes = info.inNodes.keySet,
    outNodes = info.outNodes.keySet,
    ioValues = state.ioValues.toSet.map((k, v) => (k.name, v.map(_.asHnId))),
    concreteNodes = state.graph.nodes.keySet.filter(_.isCon).map(_.asHnId),
    abstractNodes = state.graph.nodes.keySet.filter(_.isAbs).map(_.asHnId),
    edgesMapping = state.graph.structure.srcMap.toSet.map((s, ts) => (s.asHnId, ts.map(_.id.asHnId)))
  )
