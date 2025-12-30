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
| created: 2025-12-23 |||||||||||*/

package planning.engine.api

import cats.effect.kernel.Resource
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import planning.engine.api.app.AppBase
import planning.engine.api.config.MainInMemConf
import planning.engine.api.route.maintenance.MaintenanceRoute
import planning.engine.api.route.map.MapRoute
import planning.engine.api.route.visualization.VisualizationRoute
import planning.engine.api.service.maintenance.MaintenanceService
import planning.engine.api.service.map.MapInMemService
import planning.engine.api.service.visualization.VisualizationService
import planning.engine.planner.map.MapInMem


object MainRestInMem extends AppBase:
  protected override def buildApp(): Resource[IO, MaintenanceService[IO]] =
    for
      mainConf <- MainInMemConf.default[IO]

      visualizationService <- VisualizationService[IO]()
      visualizationRoute <- VisualizationRoute[IO](visualizationService)

      map <- MapInMem[IO](visualizationService)

      maintenanceService <- MaintenanceService[IO]()
      maintenanceRoute <- MaintenanceRoute[IO](maintenanceService)

      mapService <- MapInMemService[IO](map)
      mapRoute <- MapRoute[IO](mapService)

      rootRoute = maintenanceRoute.endpoints <+> mapRoute.endpoints

      _ <- buildServer(mainConf.server, ws => rootRoute <+> visualizationRoute.endpoints(ws))
    yield maintenanceService
