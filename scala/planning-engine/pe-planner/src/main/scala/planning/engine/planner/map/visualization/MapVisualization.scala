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

import cats.effect.Async
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.map.dcg.state.DcgState

trait MapVisualizationLike[F[_]]:
  def stateUpdated(state: DcgState[F]): F[Unit]

class MapVisualization[F[_]: {Async, LoggerFactory}]() extends MapVisualizationLike[F]:

  override def stateUpdated(state: DcgState[F]): F[Unit] = ???


object MapVisualization:
    def apply[F[_]: {Async, LoggerFactory}](): F[MapVisualizationLike[F]] =
        Async[F].pure(new MapVisualization[F]())