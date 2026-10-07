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
import planning.engine.api.app.mpi.{WithMap, WithVisualization}
import planning.engine.api.config.mpi.MainInMemConf
import planning.engine.api.service.maintenance.MaintenanceService
import planning.engine.planner.mpi.MapMpi

object MpiMainRestInMem extends AppBase with WithMap with WithVisualization:
  protected override def buildApp(): Resource[IO, MaintenanceService[IO]] =
    for
      conf <- MainInMemConf.default[IO]

      (vizService, vizRoute) <- buildVisualization(conf)
      maintenance <- buildMaintenance
      map <- MapMpi[IO](vizService)
      mapRoute <- buildMap(map)

      rootRoute = maintenance.route.endpoints <+> mapRoute.endpoints

      _ <- buildServer(conf.server, ws => vizRoute.map(vr => rootRoute <+> vr.endpoints(ws)).getOrElse(rootRoute))
    yield maintenance.service
