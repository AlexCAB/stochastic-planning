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

package planning.engine.api.service.map

import cats.effect.std.AtomicCell
import cats.effect.{Async, Resource}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.map.*
import cats.syntax.all.*
import planning.engine.map.{MapBuilderLike, MapGraphLake}
import planning.engine.common.errors.{assertDistinct, assertSameElems, assertionError}
import planning.engine.common.validation.Validation
import planning.engine.common.values.db.DbName
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.map.config.MapConfig

trait MapServiceLike[F[_]]:
  def getState: F[Option[(MapGraphLake[F], DbName)]]
  def reset(): F[MapResetResponse]
  def init(request: MapInitRequest): F[MapInfoResponse]
  def load(request: MapLoadRequest): F[MapInfoResponse]
  def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse]

class MapService[F[_]: {Async, LoggerFactory}](
    config: MapConfig,
    builder: MapBuilderLike[F],
    mgState: AtomicCell[F, Option[(MapGraphLake[F], DbName)]]
) extends MapServiceLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def initError(
      graph: MapGraphLake[F],
      dbName: DbName
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
        info <- MapInfoResponse.fromMapGraph(request.dbName, mapGraph)
      yield (Some(mapGraph, request.dbName), info)

    case Some((mapGraph, dbName)) => initError(mapGraph, dbName)

  override def load(request: MapLoadRequest): F[MapInfoResponse] = mgState.evalModify:
    case None =>
      for
        mapGraph <- builder.load(request.dbName, config)
        info <- MapInfoResponse.fromMapGraph(request.dbName, mapGraph)
      yield (Some(mapGraph, request.dbName), info)

    case Some((mapGraph, dbName)) if dbName == request.dbName =>
      for
          info <- MapInfoResponse.fromMapGraph(dbName, mapGraph)
      yield (Some(mapGraph, dbName), info)

    case Some((mapGraph, dbName)) => initError(mapGraph, dbName)

  override def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse] = withGraph: graph =>
    def composeHnIdMap(
        foundHnIdMap: Map[Name, List[HnId]],
        newConHnIds: Map[HnId, Option[Name]],
        newAbsHnIds: Map[HnId, Option[Name]]
    ): F[Map[Name, HnId]] =
      for
        foundIds <- foundHnIdMap.toList.traverse:
          case (name, id :: Nil) => (name -> id).pure
          case (name, ids)       => s"Expect exactly one variable with name $name, but got: $ids".assertionError
        newIds <- (newConHnIds.toList ++ newAbsHnIds.toList).traverse:
          case (id, Some(name)) => (name -> id).pure
          case (id, _)          => s"No name found for hnId: $id".assertionError
        allHnNames = foundIds ++ newIds
        _ <- allHnNames.map(_._1).assertDistinct(s"Hn names must be distinct")
      yield allHnNames.toMap

    for
      _ <- Validation.validate(definition)
      _ <- Validation.validateList(definition.samples)
      foundHnIdMap <- graph.findHnIdsByNames(definition.hnNames)
      (listNewCon, listNewAbs) <- definition.listNewNotFoundHn(foundHnIdMap.keySet, graph.getIoNode)
      newConHnIds <- graph.newConcreteNodes(listNewCon)
      newAbsHnIds <- graph.newAbstractNodes(listNewAbs)
      hnIdMap <- composeHnIdMap(foundHnIdMap, newConHnIds, newAbsHnIds)
      sampleNewList <- definition.toSampleNewList(hnIdMap)
      sampleIds <- graph.addNewSamples(sampleNewList)
      sampleNameMap <- graph.getSampleNames(sampleIds)
      _ <- (sampleIds, sampleNameMap.keys).assertSameElems("Seems bug: not for all sampleIds names found")
    yield MapAddSamplesResponse.fromSampleNames(sampleNameMap)

object MapService:
  def apply[F[_]: {Async, LoggerFactory}](
      config: MapConfig,
      builder: MapBuilderLike[F]
  ): Resource[F, MapService[F]] = Resource.eval(
    AtomicCell[F].of[Option[(MapGraphLake[F], DbName)]](None).map(mgState =>
      new MapService[F](config, builder, mgState)
    )
  )
