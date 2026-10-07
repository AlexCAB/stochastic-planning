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
| created: 2026-10-08 |||||||||||*/

package planning.engine.api.app.mpi

import cats.effect.{Async, Resource}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.mpi.MainInMemConf
import planning.engine.api.route.visualization.VisualizationRoute
import planning.engine.api.service.visualization.mpi.VisualizationMpiService
import planning.engine.planner.mpi.Visualization

trait WithVisualization:
  def buildVisualization[F[_]: {Async, LoggerFactory}](
      config: MainInMemConf,
  ): Resource[F, (Option[Visualization], Option[VisualizationRoute[F]])] =
    if config.visEnabled then
      for
        service <- VisualizationMpiService[F]()
        route <- VisualizationRoute[F](config.visRoute, service)
      yield (Some(service), Some(route))
    else
      Resource.eval(Async[F].pure((None, None)))
