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

package planning.engine.api.service.map.inmem

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.service.map.{MapServiceBase, MapServiceLike}
import planning.engine.planner.mpi.MapMpi
import planning.engine.api.model.map.*
import planning.engine.common.errors.*
import planning.engine.map.MapGraphLake
import planning.engine.common.values.db.DbName

class MapInMemMpiService[F[_]: {Async, LoggerFactory}](map: MapMpi[F])
    extends MapServiceBase[F] with MapServiceLike[F]:

  override def getState: F[Option[(MapGraphLake[F], DbName)]] = None.pure

  override def load(request: MapLoadRequest): F[MapInfoResponse] =
    "Load operation is not supported in in-memory map service".assertionError

  override def reset(): F[MapResetResponse] = ???

  override def init(request: MapInitRequest): F[MapInfoResponse] = ???

   

  override def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse] = ???


object MapInMemMpiService:
  def apply[F[_] : {Async, LoggerFactory}](map: MapMpi[F]): Resource[F, MapInMemMpiService[F]] =
    Resource.eval(new MapInMemMpiService[F](map).pure)
  