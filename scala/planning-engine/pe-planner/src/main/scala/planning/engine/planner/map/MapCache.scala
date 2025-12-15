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
import planning.engine.common.values.io.IoValue
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.map.MapGraphLake
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.sample.SampleId
import planning.engine.map.subgraph.MapSubGraph
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.state.DcgState
import planning.engine.planner.map.logic.MapBaseLogic

class MapCache[F[_]: {Async, LoggerFactory}](
    mapGraph: MapGraphLake[F],
    stateCell: AtomicCell[F, DcgState[F]]
) extends MapBaseLogic[F](stateCell) with MapLike[F]:
  private[map] def load(values: Set[IoValue], loadedSamples: Set[SampleId]): F[MapSubGraph[F]] =
    for
      subGraph <- mapGraph.loadSubgraphForIoValue(values.toList, loadedSamples.toList)
      _ <- Validation.validate(subGraph)
      _ <- (values, subGraph.allIoValues).assertContainsAll("Superfluous nodes presented")
      _ <- subGraph.abstractNodes.assertEmpty("Abstract nodes should not be loaded when loading by IoValues")
    yield subGraph

  override def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])] =
    stateCell.evalModify: state =>
      for
        notLoaded <- values.filterNot(state.ioValues.contains).pure
        subGraph <- load(notLoaded, state.samplesData.keySet)
        loadedNodes <- subGraph.concreteNodes.traverse(ConcreteDcgNode.apply)
        stateWithNodes <- state.addConcreteNodes(loadedNodes)
        loadedEdges <- subGraph.edges.traverse(DcgEdge.apply)
        stateWithEdges <- stateWithNodes.addEdges(loadedEdges)
        stateWithSamples <- stateWithEdges.addSamples(subGraph.loadedSamples)
        (foundNodes, notFoundValues) <- stateWithSamples.concreteForIoValues(values)
      yield (stateWithSamples, (foundNodes, notFoundValues))

object MapCache:
  def apply[F[_]: {Async, LoggerFactory}](mapGraph: MapGraphLake[F]): F[MapCache[F]] =
    for
        state <- AtomicCell[F].of(DcgState.init[F]())
    yield new MapCache(mapGraph, state)
