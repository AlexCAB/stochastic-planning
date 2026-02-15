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

package planning.engine.api.service.map

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.map.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.db.DbName
import planning.engine.map.MapGraphLake
import planning.engine.planner.map.MapInMemLike
import planning.engine.common.errors.*

class MapInMemService[F[_]: {Async, LoggerFactory}](map: MapInMemLike[F])
    extends MapServiceBase[F] with MapServiceLike[F]:

  override def getState: F[Option[(MapGraphLake[F], DbName)]] = None.pure

  override def load(request: MapLoadRequest): F[MapInfoResponse] =
    "Load operation is not supported in in-memory map service".assertionError

  override def init(request: MapInitRequest): F[MapInfoResponse] =
    for
      metadata <- request.toMetadata
      inputNodes <- request.toInputNodes
      outputNodes <- request.toOutputNodes
      _ <- map.init(metadata, inputNodes, outputNodes)
      info <- MapInfoResponse.emptyInMem()
    yield info.copy(
      mapName = metadata.name,
      numInputNodes = inputNodes.size,
      numOutputNodes = outputNodes.size
    )

  override def reset(): F[MapResetResponse] = map.reset().flatMap(_ => MapResetResponse.emptyInMem[F])

  override def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse] =
    for
      _ <- Validation.validate(definition)
      _ <- Validation.validateList(definition.samples)
      foundHnIdMap <- map.findHnIdsByNames(definition.hnNames.toSet).map(_.map((k, v) => k -> v.map(_.asHnId)))
      (listNewCon, listNewAbs) <- definition.listNewNotFoundHn(foundHnIdMap.keySet, map.getIoNode)
      newConIds <- map.addNewConcreteNodes(listNewCon)
      newAbsIds <- map.addNewAbstractNodes(listNewAbs)
      hnIdMap <- composeHnIdMap(foundHnIdMap, (newConIds ++ newAbsIds).map((i, n) => (i.asHnId, n)))
      sampleNewList <- definition.toSampleNewList(hnIdMap)
      samples <- map.addNewSamples(sampleNewList)
      sampleNameMap = samples.map((i, s) => (i, s.data.name))
    yield MapAddSamplesResponse.fromSampleNames(sampleNameMap)

object MapInMemService:
  def apply[F[_]: {Async, LoggerFactory}](map: MapInMemLike[F]): Resource[F, MapInMemService[F]] =
    Resource.eval(new MapInMemService[F](map).pure)
