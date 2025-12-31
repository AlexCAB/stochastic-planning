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
| created: 2025-04-19 |||||||||||*/

package planning.engine.api

import cats.effect.kernel.Resource
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import planning.engine.api.app.AppBase
import planning.engine.api.config.MainWithDbConf
import planning.engine.api.route.maintenance.MaintenanceRoute
import planning.engine.api.route.map.MapRoute
import planning.engine.api.service.maintenance.MaintenanceService
import planning.engine.api.service.map.MapWithDbService
import planning.engine.map.MapBuilder

object MainRestWithDb extends AppBase:
  protected override def buildApp(): Resource[IO, MaintenanceService[IO]] =
    for
      mainConf <- MainWithDbConf.default[IO]
      builder <- MapBuilder[IO](mainConf.db.connection)

      maintenanceService <- MaintenanceService[IO]()
      maintenanceRoute <- MaintenanceRoute(maintenanceService)

      mapService <- MapWithDbService[IO](mainConf.mapGraph, builder)
      mapRoute <- MapRoute(mapService)

      rootRoute = maintenanceRoute.endpoints <+> mapRoute.endpoints

      _ <- buildServer(mainConf.server, _ => rootRoute)
    yield maintenanceService
