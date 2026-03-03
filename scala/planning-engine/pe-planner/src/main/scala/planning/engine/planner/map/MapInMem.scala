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
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.planner.map.state.{MapGraphState, MapIdsCountState, MapInfoState}
import planning.engine.planner.map.visualization.MapVisualizationLike
import planning.engine.planner.config.PlannerMapConfig

trait MapInMemLike[F[_]] extends MapLike[F]:
  def init(metadata: MapMetadata, inNodes: Iterable[InputNode[F]], outNodes: Iterable[OutputNode[F]]): F[Unit]

class MapInMem[F[_]: {Async, LoggerFactory}](
    config: PlannerMapConfig,
    visualization: MapVisualizationLike[F],
    mapInfoCell: AtomicCell[F, MapInfoState[F]],
    dcgStateCell: AtomicCell[F, MapGraphState[F]],
    idsCountCell: AtomicCell[F, MapIdsCountState]
) extends MapBaseLogic[F](visualization, mapInfoCell, dcgStateCell) with MapInMemLike[F]:
  private val logger = LoggerFactory[F].getLogger

  private[map] def getIdsCount: F[MapIdsCountState] = idsCountCell.get
  private[map] def setIdsCount(newCount: MapIdsCountState): F[Unit] = idsCountCell.set(newCount)

  override def init(
      metadata: MapMetadata,
      inNodes: Iterable[InputNode[F]],
      outNodes: Iterable[OutputNode[F]]
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
    yield node

  private[map] def logNodesRepr(nodes: Iterable[DcgNode[F]]): F[Unit] =
    if config.reprEnabled && nodes.nonEmpty then
      for
        nodesRepr <- nodes.toList.sortBy(_.id.value).map(_.repr).pure
        _ <- logger.info(s"New nodes added to MapInMem:\n  ${nodesRepr.mkString("\n  ")}")
      yield ()
    else ().pure

  private[map] def addNodes[I <: MnId, N, M <: DcgNode[F]](
      nodes: Iterable[N],
      getIds: Int => MapIdsCountState => (MapIdsCountState, List[I]),
      makeNode: (I, N) => F[M]
  ): F[Map[MnId, Option[HnName]]] =
    for
      mnIds <- idsCountCell.modify(getIds(nodes.size))
      _ <- mnIds.assertSameSize(nodes, "Seems bug: HnIds count does not match concrete nodes count")
      nodesWithId = nodes.zip(mnIds)
      dsgNodes <- nodesWithId.toList.traverse((node, mnId) => makeNode(mnId, node))
      _ <- modifyMapState(_.addNodes(dsgNodes).map(ns => (ns, ())))
      _ <- logger.info(s"Added new nodes to MapInMem: $nodesWithId")
      _ <- logNodesRepr(dsgNodes)
    yield dsgNodes.map(n => n.id -> n.name).toMap

  override def addNewConcreteNodes(nodes: ConcreteNode.ListNew): F[Map[MnId, Option[HnName]]] =
    addNodes[MnId.Con, ConcreteNode.New, DcgNode.Concrete[F]](
      nodes.list,
      i => _.getNextConIds(i),
      (mnId, node) => DcgNode.Concrete(mnId, node, n => getIoNode(n))
    )

  override def addNewAbstractNodes(nodes: AbstractNode.ListNew): F[Map[MnId, Option[HnName]]] =
    addNodes[MnId.Abs, AbstractNode.New, DcgNode.Abstract[F]](
      nodes.list,
      i => _.getNextAbsIds(i),
      (mnId, node) => DcgNode.Abstract(mnId, node)
    )

  private[map] def buildDcgSampleAdd(
      sampleId: SampleId,
      sample: Sample.New,
      state: MapGraphState[F]
  ): F[DcgSample.Add[F]] =
    for
      dcgSample <- DcgSample(sampleId, sample, state.graph.conMnId, state.graph.absMnId)
      indexiesMap <- idsCountCell.modify(_.getNextHnIndexes(dcgSample.structure.mnIds))
    yield DcgSample.Add(dcgSample, indexiesMap)

  private[map] def buildSamples(samples: Sample.ListNew)(state: MapGraphState[F]): F[Iterable[DcgSample.Add[F]]] =
    for
      sampleIds <- idsCountCell.modify(_.getNextSampleIds(samples.list.size))
      _ <- sampleIds.assertDistinct("Generated sample IDs are not distinct")
      _ <- sampleIds.assertSameSize(samples.list, "Sample IDs count does not match samples count")
      sampleMap = sampleIds.zip(samples.list)
      samplesAdd <- sampleMap.traverse((id, sample) => buildDcgSampleAdd(id, sample, state))
    yield samplesAdd

  private[map] def logSamplesRepr(samples: Iterable[DcgSample[F]]): F[Unit] =
    if config.reprEnabled && samples.nonEmpty then
      for
        samplesRepr <- samples.toList.traverse(_.repr).map(_.mkString("\n"))
        graphRepr <- getMapState.flatMap(_.graph.repr)
        _ <- logger.info(s"New samples added to MapInMem:\n$samplesRepr\nCurrent graph state:\n$graphRepr")
      yield ()
    else ().pure

  override def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, DcgSample[F]]] =
    for
      samplesMap <- addNewSamplesToState(buildSamples(samples))
      _ <- logSamplesRepr(samplesMap.values)
    yield samplesMap

  override def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[MnId]]] =
    for
      state <- getMapState
      resultMap = state.graph.findHnIdsByNames(names)
    yield resultMap

  override def findForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    for
      state <- getMapState
      (foundNodes, notFoundValues) <- state.findConForIoValues(values)
    yield (foundNodes, notFoundValues)

// TODO Refactoring:

//  override def findActiveAbstractForest(conActiveHnIds: Set[MnId]): F[ActiveAbsDag[F]] =
//    for
//      state <- getMapState
//      initGraph <- buildInitActiveGraph(conActiveHnIds, state)
//      tracedGraph <- traceActiveAbsNodes(initGraph, Set(), state)
//      _ <- Validation.validate(tracedGraph)
//      _ <- logger.info(s"Found active abstract graph for conActiveNodeIds=$conActiveHnIds: $tracedGraph")
//    yield tracedGraph

  override def reset(): F[Unit] =
    for
      info <- getMapInfo
      _ <- mapInfoCell.set(MapInfoState.empty[F])
      _ <- dcgStateCell.set(MapGraphState.empty[F])
      _ <- idsCountCell.set(MapIdsCountState.init)
      _ <- logger.info(s"Resetting MapInMem with current MapInfoState: $info")
    yield ()

object MapInMem:
  def empty[F[_]: {Async,
    LoggerFactory}](config: PlannerMapConfig, visualization: MapVisualizationLike[F]): F[MapInMem[F]] =
    for
      mapInfo <- AtomicCell[F].of(MapInfoState.empty[F])
      dcgState <- AtomicCell[F].of(MapGraphState.empty[F])
      idsCount <- AtomicCell[F].of(MapIdsCountState.init)
    yield new MapInMem(config, visualization, mapInfo, dcgState, idsCount)

  def apply[F[_]: {Async,
    LoggerFactory}](config: PlannerMapConfig, visualization: MapVisualizationLike[F]): Resource[F, MapInMem[F]] =
    Resource.eval(empty(config, visualization))
