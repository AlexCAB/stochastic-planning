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
import planning.engine.planner.map.dcg.nodes.{AbstractDcgNode, ConcreteDcgNode}
import planning.engine.planner.map.dcg.state.{DcgState, IdsCountState, MapInfoState}
import planning.engine.planner.map.logic.MapBaseLogic
import planning.engine.common.errors.*
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.planner.map.visualization.MapVisInLike

trait MapInMemLike[F[_]] extends MapLike[F]:
  def init(metadata: MapMetadata, inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[Unit]

class MapInMem[F[_]: {Async, LoggerFactory}](
    visualization: MapVisInLike[F],
    mapInfoCell: AtomicCell[F, MapInfoState[F]],
    dcgStateCell: AtomicCell[F, DcgState[F]],
    idsCountCell: AtomicCell[F, IdsCountState]
) extends MapBaseLogic[F](visualization, dcgStateCell) with MapInMemLike[F]:
  private val logger = LoggerFactory[F].getLogger

  private[map] def getMapInfo: F[MapInfoState[F]] = mapInfoCell.get
  private[map] def getIdsCount: F[IdsCountState] = idsCountCell.get

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
        _ <- dcgStateCell.set(DcgState.empty[F])
        _ <- idsCountCell.set(IdsCountState.init)
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
      hnIdIds <- idsCountCell.modify(_.getNextHdIds(nodes.list.size))
      _ <- (hnIdIds, nodes.list).assertSameSize("Seems bug: HnIds count does not match concrete nodes count")
      dsgNodes <- nodes.list.zip(hnIdIds)
        .traverse((node, hnId) => ConcreteDcgNode(hnId, node, n => getMapInfo.flatMap(_.getIoNode(n))))
      _ <- modifyMapState(_.addConcreteNodes(dsgNodes).map(ns => (ns, ())))
      _ <- logger.info(s"Added new concrete nodes to MapInMem: ${nodes.list.zip(hnIdIds)}")
    yield dsgNodes.map(n => n.id -> n.name).toMap

  override def addNewAbstractNodes(nodes: AbstractNode.ListNew): F[Map[HnId, Option[HnName]]] =
    for
      hnIdIds <- idsCountCell.modify(_.getNextHdIds(nodes.list.size))
      _ <- (hnIdIds, nodes.list).assertSameSize("Seems bug: HnIds count does not match abstract nodes count")
      dsgNodes <- nodes.list.zip(hnIdIds).traverse((node, hnId) => AbstractDcgNode(hnId, node))
      _ <- modifyMapState(_.addAbstractNodes(dsgNodes).map(ns => (ns, ())))
      _ <- logger.info(s"Added new abstract nodes to MapInMem: ${nodes.list.zip(hnIdIds)}")
    yield dsgNodes.map(n => n.id -> n.name).toMap

  override def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, Sample]] =
    addNewSamplesToCache(buildSamples(samples))

  override def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[HnId]]] =
    for
      state <- getMapState
      result <- state.findHnIdsByNames(names)
      _ <- logger.info(s"Found HnIds by names in mem: $result")
    yield result

  override def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])] =
    for
      state <- getMapState
      (foundNodes, notFoundValues) <- state.concreteForIoValues(values)
      _ <- logger.info(s"Got from map in mem: foundNodes = $foundNodes, notFoundValues = $notFoundValues")
    yield (foundNodes, notFoundValues)

  override def reset(): F[Unit] =
    for
      info <- getMapInfo
      _ <- mapInfoCell.set(MapInfoState.empty[F])
      _ <- dcgStateCell.set(DcgState.empty[F])
      _ <- idsCountCell.set(IdsCountState.init)
      _ <- logger.info(s"Resetting MapInMem with current MapInfoState: $info")
    yield ()

object MapInMem:
  def init[F[_]: {Async, LoggerFactory}](visualization: MapVisInLike[F]): F[MapInMem[F]] =
    for
      mapInfo <- AtomicCell[F].of(MapInfoState.empty[F])
      dcgState <- AtomicCell[F].of(DcgState.empty[F])
      idsCount <- AtomicCell[F].of(IdsCountState.init)
    yield new MapInMem(visualization, mapInfo, dcgState, idsCount)

  def apply[F[_]: {Async, LoggerFactory}](visualization: MapVisInLike[F]): Resource[F, MapInMem[F]] =
    Resource.eval(init(visualization))
