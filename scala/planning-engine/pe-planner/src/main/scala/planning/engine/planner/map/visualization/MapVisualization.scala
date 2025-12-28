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

import cats.effect.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.config.MapVisualizationConfig
import planning.engine.planner.map.dcg.state.DcgState
import planning.engine.planner.map.visualization.data.GraphMapVis

trait MapVisInLike[F[_]]:
  def stateUpdated(state: DcgState[F]): F[Unit]

trait MapVisOutLike[F[_]]:
  def graphLongPull(): F[GraphMapVis]

class MapVisualization[F[_]: {Async, LoggerFactory}](
    //mapInfoCell: AtomicCell[F, List[Int]],
    config: MapVisualizationConfig
) extends MapVisInLike[F] with MapVisOutLike[F]:

  private val logger = LoggerFactory[F].getLogger

  val t = IO.pure(10)
  


//  timeoutTo()


  override def stateUpdated(state: DcgState[F]): F[Unit] = ???

  override def graphLongPull(): F[GraphMapVis] = ???

object MapVisualization:
  private[map] def init[F[_]: {Async, LoggerFactory}](config: MapVisualizationConfig): F[MapVisInLike[F]] =
    Async[F].delay(new MapVisualization[F](config))

  def apply[F[_]: {Async, LoggerFactory}](config: MapVisualizationConfig): Resource[F, MapVisInLike[F]] =
    Resource.eval(init(config))
