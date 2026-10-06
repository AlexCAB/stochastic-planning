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
| created: 2025-12-28 |||||||||||*/

package planning.engine.api.service.visualization

import fs2.{Pipe, Stream}
import planning.engine.api.model.visualization.VisualizationMsg

trait VisualizationService[F[_]]:
  def mapSendWs: Stream[F, VisualizationMsg]
  def mapReceiveWs: Pipe[F, String, Unit]
