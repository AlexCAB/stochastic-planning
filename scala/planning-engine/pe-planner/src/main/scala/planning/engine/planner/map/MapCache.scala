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
| created: 2025-11-30 |||||||||||*/

package planning.engine.planner.map

import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import planning.engine.common.values.io.{IoName, IoValue}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.MapGraphLake
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.map.samples.sample.Sample
import planning.engine.map.subgraph.MapSubGraph
import planning.engine.planner.map.dcg.ActiveAbstractGraph
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.planner.map.dcg.state.{DcgState, MapInfoState}
import planning.engine.planner.map.logic.MapBaseLogic
import planning.engine.planner.map.visualization.MapVisualizationLike

class MapCache[F[_]: {Async, LoggerFactory}](
    mapGraph: MapGraphLake[F],
    visualization: MapVisualizationLike[F],
    mapInfoCell: AtomicCell[F, MapInfoState[F]],
    stateCell: AtomicCell[F, DcgState[F]]
) extends MapBaseLogic[F](visualization, mapInfoCell, stateCell) with MapLike[F]:
  private val logger = LoggerFactory[F].getLogger

  private[map] def load(values: Set[IoValue], loadedSamples: Set[SampleId]): F[MapSubGraph[F]] =
    for
      subGraph <- mapGraph.loadSubgraphForIoValue(values.toList, loadedSamples.toList)
      _ <- Validation.validate(subGraph)
      _ <- (values, subGraph.allIoValues).assertContainsAll("Superfluous nodes presented")
      _ <- subGraph.abstractNodes.assertEmpty("Abstract nodes should not be loaded when loading by IoValues")
    yield subGraph

  override def getIoNode(name: IoName): F[IoNode[F]] = ???

  override def addNewConcreteNodes(params: ConcreteNode.ListNew): F[Map[HnId, Option[HnName]]] = ???

  override def addNewAbstractNodes(params: AbstractNode.ListNew): F[Map[HnId, Option[HnName]]] = ???

  override def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, Sample]] =
    addNewSamplesToCache(mapGraph.addNewSamples(samples))

  override def findHnIdsByNames(names: Set[HnName]): F[Map[HnName, Set[HnId]]] = ???

  override def findForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[DcgNode.Concrete[F]]], Set[IoValue])] =
    modifyMapState: state =>
      for
        notLoaded <- values.filterNot(state.ioValues.contains).pure
        subGraph <- load(notLoaded, state.samplesData.keySet)
        loadedNodes <- subGraph.concreteNodes.traverse(DcgNode.Concrete.apply)
        stateWithNodes <- state.addConcreteNodes(loadedNodes)
        loadedEdges = subGraph.edges.map(DcgEdgeData.apply)
        stateWithEdges <- stateWithNodes.addEdges(loadedEdges)
        stateWithSamples <- stateWithEdges.addSamples(subGraph.loadedSamples)
        (foundNodes, notFoundValues) <- stateWithSamples.concreteForIoValues(values)
        _ <- logger.info(s"For IO values: found = $foundNodes, notFound = $notFoundValues, loaded = $loadedNodes")
      yield (stateWithSamples, (foundNodes, notFoundValues))

  override def findActiveAbstractGraph(concreteNodeIds: Set[HnId]): F[Set[ActiveAbstractGraph[F]]] = ??? 

  override def reset(): F[Unit] = ???

object MapCache:
  def apply[F[_]: {Async, LoggerFactory}](
      mapGraph: MapGraphLake[F],
      visualization: MapVisualizationLike[F]
  ): F[MapCache[F]] =
    for
      mapInfo <- AtomicCell[F].of(MapInfoState.empty[F])
      state <- AtomicCell[F].of(DcgState.empty[F])
    yield new MapCache(mapGraph, visualization, mapInfo, state)
