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
| created: 2026-09-16 |||||||||||*/

package planning.engine.api

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.implicits.toSemigroupKOps
import planning.engine.api.app.AppBase
import planning.engine.api.config.mpi.MainInMemConf
import planning.engine.api.route.map.MapRoute
import planning.engine.api.service.maintenance.MaintenanceService
import planning.engine.planner.mpi.MapMpi
import planning.engine.api.service.map.inmem.MapInMemMpiService

object MpiMainRestInMem extends AppBase:
  protected override def buildApp(): Resource[IO, MaintenanceService[IO]] =
    for
      conf <- MainInMemConf.default[IO]

//      visualizationService <- VisualizationService[IO](conf.visService)
//      visualizationRoute <- VisualizationRoute[IO](conf.visRoute, visualizationService)

      maintenance <- buildMaintenance

      map <- MapMpi[IO](visualization = None) // TODO implement visualization for MPI

      mapService <- MapInMemMpiService[IO](map)
      mapRoute <- MapRoute[IO](mapService)

      rootRoute = maintenance.route.endpoints <+> mapRoute.endpoints

      _ <- buildServer(conf.server, ws => rootRoute)
    yield maintenance.service
