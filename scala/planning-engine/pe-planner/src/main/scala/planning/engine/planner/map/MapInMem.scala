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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map

import cats.effect.Resource
import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.io.{IoName, IoValue}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.logic.MapBaseLogic
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.planner.map.dcg.ActiveAbsDag
import planning.engine.planner.map.state.{MapGraphState, MapIdsCountState, MapInfoState}
import planning.engine.planner.map.visualization.MapVisualizationLike

trait MapInMemLike[F[_]] extends MapLike[F]:
  def init(metadata: MapMetadata, inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[Unit]

class MapInMem[F[_]: {Async, LoggerFactory}](
    visualization: MapVisualizationLike[F],
    mapInfoCell: AtomicCell[F, MapInfoState[F]],
    dcgStateCell: AtomicCell[F, MapGraphState[F]],
    idsCountCell: AtomicCell[F, MapIdsCountState]
) extends MapBaseLogic[F](visualization, mapInfoCell, dcgStateCell) with MapInMemLike[F]:
  private val logger = LoggerFactory[F].getLogger
  private[map] def getIdsCount: F[MapIdsCountState] = idsCountCell.get

  private[map] def buildSamples(newSamples: Sample.ListNew): F[List[Sample]] =
    for
      sampleIds <- idsCountCell.modify(_.getNextSampleIds(newSamples.list.size))
      _ <- sampleIds.assertDistinct("Generated sample IDs are not distinct")
      _ <- (sampleIds, newSamples.list).assertSameSize("Sample IDs count does not match samples count")
      sampleMap = sampleIds.zip(newSamples.list)
      sampleWithHnIndexMap <- sampleMap.traverse: (id, sample) =>
        idsCountCell.modify(_.getNextHnIndexes(sample.hnIds)).map(ixs => (id, sample, ixs))
      samples <- sampleWithHnIndexMap.traverse((id, sample, indexes) => Sample.formNew(id, sample, indexes))
    yield samples

  override def init(
      metadata: MapMetadata,
      inNodes: List[InputNode[F]],
      outNodes: List[OutputNode[F]]
  ): F[Unit] = mapInfoCell.evalModify(info =>
    if info.isEmpty then
      for
        info <- MapInfoState[F](metadata, inNodes, outNodes)
        _ <- dcgStateCell.set(MapGraphState.empty[F])
        _ <- idsCountCell.set(MapIdsCountState.init)
        state <- dcgStateCell.get
        _ <- visualization.stateUpdated(info, state)
        _ <- logger.info(s"Initialized MapInMem with metadata: $metadata")
      yield (info, ())
    else
      s"MapInMem is already initialized and cannot be initialized again".assertionError
  )

  override def getIoNode(name: IoName): F[IoNode[F]] =
    for
      info <- getMapInfo
      node <- info.getIoNode(name)
      _ <- logger.info(s"Got IO node from MapInMem: $node")
    yield node

  override def addNewConcreteNodes(nodes: ConcreteNode.ListNew): F[Map[HnId, Option[HnName]]] =
    for
      hnIdIds <- idsCountCell.modify(_.getNextHnIds(nodes.list.size))
      _ <- (hnIdIds, nodes.list).assertSameSize("Seems bug: HnIds count does not match concrete nodes count")
      dsgNodes <- nodes.list.zip(hnIdIds)
        .traverse((node, hnId) => DcgNode.Concrete(hnId, node, n => getMapInfo.flatMap(_.getIoNode(n))))
      _ <- modifyMapState(_.addConcreteNodes(dsgNodes).map(ns => (ns, ())))
      _ <- logger.info(s"Added new concrete nodes to MapInMem: ${nodes.list.zip(hnIdIds)}")
    yield dsgNodes.map(n => n.id -> n.name).toMap

  override def addNewAbstractNodes(nodes: AbstractNode.ListNew): F[Map[HnId, Option[HnName]]] =
    for
      hnIdIds <- idsCountCell.modify(_.getNextHnIds(nodes.list.size))
      _ <- (hnIdIds, nodes.list).assertSameSize("Seems bug: HnIds count does not match abstract nodes count")
      dsgNodes <- nodes.list.zip(hnIdIds).traverse((node, hnId) => DcgNode.Abstract(hnId, node))
      _ <- modifyMapState(_.addAbstractNodes(dsgNodes).map(ns => (ns, ())))
      _ <- logger.info(s"Added new abstract nodes to MapInMem: ${nodes.list.zip(hnIdIds)}")
    yield dsgNodes.map(n => n.id -> n.name).toMap

  override def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, Sample]] =
    addNewSamplesToCache(buildSamples(samples))

  override def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[HnId]]] =
    for
      state <- getMapState
      result <- state.graph.findHnIdsByNames(names)
      _ <- logger.info(s"Found HnIds by names in mem: $result")
    yield result

  override def findForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    for
      state <- getMapState
      (foundNodes, notFoundValues) <- state.findConForIoValues(values)
      _ <- logger.info(s"Got from map in mem: foundNodes = $foundNodes, notFoundValues = $notFoundValues")
    yield (foundNodes, notFoundValues)

  override def findActiveAbstractForest(conActiveHnIds: Set[HnId]): F[ActiveAbsDag[F]] =
    for
      state <- getMapState
      initGraph <- buildInitActiveGraph(conActiveHnIds, state)
      tracedGraph <- traceActiveAbsNodes(initGraph, Set(), state)
      _ <- Validation.validate(tracedGraph)
      _ <- logger.info(s"Found active abstract graph for conActiveNodeIds=$conActiveHnIds: $tracedGraph")
    yield tracedGraph

  override def reset(): F[Unit] =
    for
      info <- getMapInfo
      _ <- mapInfoCell.set(MapInfoState.empty[F])
      _ <- dcgStateCell.set(MapGraphState.empty[F])
      _ <- idsCountCell.set(MapIdsCountState.init)
      _ <- logger.info(s"Resetting MapInMem with current MapInfoState: $info")
    yield ()

object MapInMem:
  def init[F[_]: {Async, LoggerFactory}](visualization: MapVisualizationLike[F]): F[MapInMem[F]] =
    for
      mapInfo <- AtomicCell[F].of(MapInfoState.empty[F])
      dcgState <- AtomicCell[F].of(MapGraphState.empty[F])
      idsCount <- AtomicCell[F].of(MapIdsCountState.init)
    yield new MapInMem(visualization, mapInfo, dcgState, idsCount)

  def apply[F[_]: {Async, LoggerFactory}](visualization: MapVisualizationLike[F]): Resource[F, MapInMem[F]] =
    Resource.eval(init(visualization))
