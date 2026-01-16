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
| created: 2025-12-26 |||||||||||*/

package planning.engine.planner.map.visualization

import planning.engine.planner.map.state.{MapGraphState, MapInfoState}

trait MapVisualizationLike[F[_]]:
  def stateUpdated(info: MapInfoState[F], state: MapGraphState[F]): F[Unit]
