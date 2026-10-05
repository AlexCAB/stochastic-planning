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
import planning.engine.api.service.map.{MapServiceBase, MapService}
import planning.engine.planner.mpi.MapMpi
import planning.engine.api.model.map.*
import planning.engine.common.errors.*
import planning.engine.map.MapGraphLake
import planning.engine.common.values.db.DbName
import planning.engine.api.model.map.extensions.mpi.*
import planning.engine.common.validation.Validation

class MapInMemMpiService[F[_]: {Async, LoggerFactory}](map: MapMpi[F])
    extends MapServiceBase[F] with MapService[F]:

  import MapInitRequestEx.*, MapAddSamplesRequestEx.*, MapAddSamplesResponseEx.*
  private val logger = LoggerFactory[F].getLogger

  override def getState: F[Option[(MapGraphLake[F], DbName)]] = None.pure

  override def load(request: MapLoadRequest): F[MapInfoResponse] =
    "Load operation is not supported in in-memory map service".assertionError

  override def reset(): F[MapResetResponse] = map.reset().map(_ => MapResetResponse.emptyInMem)

  override def init(request: MapInitRequest): F[MapInfoResponse] =
    for
      metadata <- request.metadata
      inVars <- request.inVars
      outVars <- request.outVars
      _ <- map.init(metadata, inVars, outVars)
      _ <- logger.info(s"Map initialized with metadata = $metadata")
    yield MapInfoResponse.emptyInMem.copy(
      mapName = Some(metadata.name),
      numInputNodes = inVars.size,
      numOutputNodes = outVars.size,
    )

  override def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse] =
    for
      _ <- Validation.validate(definition)
      _ <- Validation.validateList(definition.samples)
      (mnIds, nodes) <- definition.toNodes
      _ <- logger.info(s"Built mnIds map = $mnIds")
      samples <- definition.toSamples(mnIds)
      ids <- map.addSamples(samples, nodes)
      _ <- logger.info(s"Added samples = $ids")
      response <- ids.toMapAddSamplesResponse
    yield response

object MapInMemMpiService:
  def apply[F[_]: {Async, LoggerFactory}](map: MapMpi[F]): Resource[F, MapInMemMpiService[F]] =
    Resource.eval(new MapInMemMpiService[F](map).pure)
