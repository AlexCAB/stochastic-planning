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
import planning.engine.api.route.map.MapRoute
import planning.engine.api.service.map.mpi.MapInMemMpiService
import planning.engine.planner.mpi.MapMpi

trait WithMap:
  def buildMap[F[_]: {Async, LoggerFactory}](map: MapMpi[F]): Resource[F, MapRoute[F]] =
    for
      mapService <- MapInMemMpiService[F](map)
      mapRoute <- MapRoute[F](mapService)
    yield mapRoute
