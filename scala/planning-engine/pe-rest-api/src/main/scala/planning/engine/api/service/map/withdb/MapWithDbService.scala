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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.service.map.withdb

import cats.effect.std.AtomicCell
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.map.*
import planning.engine.api.service.map.{MapServiceBase, MapServiceLike}
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.db.DbName
import planning.engine.map.config.MapConfig
import planning.engine.map.{MapBuilderLike, MapGraphLake}
import planning.engine.api.model.map.extensions.gsi.MapInitRequestEx
import planning.engine.api.model.map.extensions.gsi.MapGraphLakeEx

class MapWithDbService[F[_]: {Async, LoggerFactory}](
    config: MapConfig,
    builder: MapBuilderLike[F],
    mgState: AtomicCell[F, Option[(MapGraphLake[F], DbName)]],
) extends MapServiceBase[F] with MapServiceLike[F]:
  import MapInitRequestEx.*, MapGraphLakeEx.*

  private val logger = LoggerFactory[F].getLogger

  private def initError(
      graph: MapGraphLake[F],
      dbName: DbName,
  ): F[(Option[(MapGraphLake[F], DbName)], MapInfoResponse)] =
    for
      msg <- s"Map graph already initialized, graph = $graph, dbName = $dbName".pure[F]
      _ <- logger.error(msg)
      err <- msg.assertionError[F, (Option[(MapGraphLake[F], DbName)], MapInfoResponse)]
    yield err

  private def withGraph[R](block: MapGraphLake[F] => F[R]): F[R] = mgState.get.flatMap:
    case Some((mapGraph, _)) => block(mapGraph)
    case None                => "Map graph is not initialized".assertionError[F, R]

  override def getState: F[Option[(MapGraphLake[F], DbName)]] = mgState.get

  override def reset(): F[MapResetResponse] = mgState.modify:
    case None                     => (None, MapResetResponse(None, None))
    case Some((mapGraph, dbName)) => (None, MapResetResponse(Some(dbName), mapGraph.metadata.name))

  override def init(request: MapInitRequest): F[MapInfoResponse] = mgState.evalModify:
    case None =>
      for
        metadata <- request.toMetadata
        inputNodes <- request.toInputNodes
        outputNodes <- request.toOutputNodes
        mapGraph <- builder.init(request.dbName, config, metadata, inputNodes, outputNodes)
        info <- mapGraph.toResponse(request.dbName)
      yield (Some(mapGraph, request.dbName), info)

    case Some((mapGraph, dbName)) => initError(mapGraph, dbName)

  override def load(request: MapLoadRequest): F[MapInfoResponse] = mgState.evalModify:
    case None =>
      for
        mapGraph <- builder.load(request.dbName, config)
        info <- mapGraph.toResponse(request.dbName)
      yield (Some(mapGraph, request.dbName), info)

    case Some((mapGraph, dbName)) if dbName == request.dbName =>
      for
          info <- mapGraph.toResponse(dbName)
      yield (Some(mapGraph, dbName), info)

    case Some((mapGraph, dbName)) => initError(mapGraph, dbName)

  override def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse] = withGraph: graph =>
    for
      _ <- Validation.validate(definition)
      _ <- Validation.validateList(definition.samples)
      foundHnIdMap <- graph.findHnIdsByNames(definition.hnNames)
      (listNewCon, listNewAbs) <- definition.listNewNotFoundHn(foundHnIdMap.keySet, graph.getIoNode)
      newConHnIds <- graph.newConcreteNodes(listNewCon)
      newAbsHnIds <- graph.newAbstractNodes(listNewAbs)
      hnIdMap <- composeHnIdMap(foundHnIdMap.map((k, v) => k -> v.toSet), newConHnIds ++ newAbsHnIds)
      sampleNewList <- definition.toSampleNewList(hnIdMap)
      sampleIds <- graph.addNewSamples(sampleNewList).map(_.map(_.data.id))
      sampleNameMap <- graph.getSampleNames(sampleIds)
      _ <- sampleIds.assertSameElems(sampleNameMap.keys, "Seems bug: not for all sampleIds names found")
    yield MapAddSamplesResponse.fromSampleNames(sampleNameMap)

object MapWithDbService:
  def apply[F[_]: {Async, LoggerFactory}](
      config: MapConfig,
      builder: MapBuilderLike[F],
  ): Resource[F, MapWithDbService[F]] = Resource.eval(
    AtomicCell[F].of[Option[(MapGraphLake[F], DbName)]](None).map(mgState =>
      new MapWithDbService[F](config, builder, mgState),
    ),
  )
