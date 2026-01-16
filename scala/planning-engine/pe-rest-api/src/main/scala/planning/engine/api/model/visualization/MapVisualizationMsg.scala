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
import planning.engine.planner.map.dcg.state.{DcgState, MapInfoState}

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

  def fromState[F[_]](info: MapInfoState[F], state: DcgState[F]): MapVisualizationMsg = MapVisualizationMsg(
    inNodes = info.inNodes.keySet,
    outNodes = info.outNodes.keySet,
    ioValues = state.ioValues.toSet.map((k, v) => (k.name, v)),
    concreteNodes = state.concreteNodes.keySet,
    abstractNodes = state.abstractNodes.keySet,
    edgesMapping = state.edgesMapping.forward.toSet
  )
