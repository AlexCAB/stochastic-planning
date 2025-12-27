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
import planning.engine.api.service.maintenance.MaintenanceService
import planning.engine.api.service.map.MapInMemService
import planning.engine.planner.map.MapInMem
import planning.engine.planner.map.visualization.MapVisualization

object MainRestInMem extends AppBase:
  protected override def buildApp(): Resource[IO, MaintenanceService[IO]] =
    for
      mainConf <- MainInMemConf.default[IO]
      visualization <- Resource.eval(MapVisualization[IO]())
      map <- Resource.eval(MapInMem[IO](visualization))

      maintenanceService <- MaintenanceService[IO]()
      maintenanceRoute <- MaintenanceRoute[IO](maintenanceService)

      mapService <- MapInMemService[IO](map)
      mapRoute <- MapRoute[IO](mapService)

      rootRoute = maintenanceRoute.endpoints <+> mapRoute.endpoints

      _ <- buildServer(mainConf.server, rootRoute)
    yield maintenanceService
