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
import planning.engine.map.graph.{MapBuilderLike, MapConfig, MapGraphLake}
import planning.engine.common.errors.{assertSameElems, assertionError, assertDistinct}
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name

trait MapServiceLike[F[_]]:
  def init(definition: MapInitRequest): F[MapInfoResponse]
  def load: F[MapInfoResponse]
  def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse]

class MapService[F[_]: {Async, LoggerFactory}](
    config: MapConfig,
    builder: MapBuilderLike[F],
    mgState: AtomicCell[F, Option[MapGraphLake[F]]]
) extends MapServiceLike[F]:

  private val logger = LoggerFactory[F].getLogger

  private def initError(graph: MapGraphLake[F]): F[(Option[MapGraphLake[F]], MapInfoResponse)] =
    for
      _ <- logger.error(s"Knowledge graph already initialized, overwriting, $graph")
      err <- "Knowledge graph already initialized".assertionError[F, (Option[MapGraphLake[F]], MapInfoResponse)]
    yield err

  private def withGraph[R](block: MapGraphLake[F] => F[R]): F[R] = mgState.get.flatMap:
    case Some(kg) => block(kg)
    case None     => "Map graph is not initialized".assertionError[F, R]

  override def init(definition: MapInitRequest): F[MapInfoResponse] = mgState.evalModify:
    case None =>
      for
        metadata <- definition.toMetadata
        inputNodes <- definition.toInputNodes
        outputNodes <- definition.toOutputNodes
        knowledgeGraph <- builder.init(config, metadata, inputNodes, outputNodes)
        info <- MapInfoResponse.fromKnowledgeGraph(knowledgeGraph)
      yield (Some(knowledgeGraph), info)

    case Some(kg) => initError(kg)

  override def load: F[MapInfoResponse] = mgState.evalModify:
    case None =>
      for
        knowledgeGraph <- builder.load(config)
        info <- MapInfoResponse.fromKnowledgeGraph(knowledgeGraph)
      yield (Some(knowledgeGraph), info)

    case Some(kg) => initError(kg)

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
        newNames = newConHnIds.toList ++ newAbsHnIds.toList
        newIds <- newNames.traverse:
          case (id, Some(name)) => (name -> id).pure
          case (id, _)          => s"No name found for hnId: $id".assertionError
        allHnNames = foundIds ++ newIds
        _ <- allHnNames.map(_._1).assertDistinct(s"Hn names must be distinct")
      yield allHnNames.toMap

    for
      _ <- Validation.validateList(definition.samples)
      foundHnIdMap <- graph.findHnIdsByNames(definition.hnNames)
      (listNewCon, listNewAbs) = definition.listNewNotFoundHn(foundHnIdMap.keySet)
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
    AtomicCell[F].of[Option[MapGraphLake[F]]](None).map(mgState => new MapService[F](config, builder, mgState))
  )
