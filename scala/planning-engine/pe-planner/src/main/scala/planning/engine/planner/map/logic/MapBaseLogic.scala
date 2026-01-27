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

package planning.engine.planner.map.logic

import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.Sample
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.ActiveAbsDag
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.common.values.edges.EndIds
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}
import planning.engine.planner.map.visualization.MapVisualizationLike

abstract class MapBaseLogic[F[_]: {Async, LoggerFactory}](
    visualization: MapVisualizationLike[F],
    mapInfoCell: AtomicCell[F, MapInfoState[F]],
    stateCell: AtomicCell[F, MapGraphState[F]]
):
  private[map] def getMapInfo: F[MapInfoState[F]] = mapInfoCell.get
  private[map] def setMapInfo(info: MapInfoState[F]): F[Unit] = mapInfoCell.set(info)
  private[map] def getMapState: F[MapGraphState[F]] = stateCell.get
  private[map] def setMapState(state: MapGraphState[F]): F[Unit] = stateCell.set(state)

  private[map] def modifyMapState[R](proc: MapGraphState[F] => F[(MapGraphState[F], R)]): F[R] =
    for
      (state, res) <- stateCell.evalModify(s => proc(s).map((ns, r) => (ns, (ns, r))))
      info <- mapInfoCell.get
      _ <- visualization.stateUpdated(info, state)
    yield res

  private[map] def addNewSamplesToCache(newSamples: => F[List[Sample]]): F[Map[SampleId, Sample]] = modifyMapState:
    state =>
      for
        samples <- newSamples
        _ <- Validation.validateList(samples)
        _ <- state.graph.allHnIds.assertContainsAll(samples.flatMap(_.allHnIds), "New samples contain unknown HnIds")
        allEdges = samples.flatMap(_.edges.toList)
        groupedEdges = allEdges.groupBy(e => (e.edgeType, EndIds(e.source.hnId, e.target.hnId))).toList
        dcgEdges <- groupedEdges.traverse((k, es) => DcgEdgeData(k._1, k._2, es))
        stateWithEdges <- state.mergeEdges(dcgEdges)
        stateWithSamples <- stateWithEdges.addSamplesData(samples.map(_.data))
      yield (stateWithSamples, samples.map(s => s.data.id -> s).toMap)

  private[map] def buildInitActiveGraph(conActiveHnIds: Set[HnId], state: MapGraphState[F]): F[ActiveAbsDag[F]] =
    for
      conNodes <- state.graph.getConForHnIds(conActiveHnIds)
      linkEdges <- state.graph.findForwardLinkEdges(conNodes.keySet)
      absNodes <- state.graph.getAbsForHnIds(linkEdges.keySet.map(_.trg))
      thenEdges <- state.graph.findBackwardThenEdges(conActiveHnIds)
      sampleIds = (thenEdges.values.flatMap(_.sampleIds) ++ thenEdges.values.flatMap(_.sampleIds)).toSet
      samples <- state.graph.getSamples(thenEdges.values.flatMap(e => e.linksIds ++ e.thensIds).toSet)
      newDag <- ActiveAbsDag(conNodes.values, absNodes.values, linkEdges.values, thenEdges.keySet, samples.values)
    yield newDag

  private[map] def traceActiveAbsNodes(
      dag: ActiveAbsDag[F],
      nextAbsHnIds: Set[HnId], // next abstract HnIds to trace which data already in active graph
      state: MapGraphState[F]
  ): F[ActiveAbsDag[F]] =
    for
      sampleIds <- dag.graph.samplesData.keySet.pure
      forwardLinkEdges <- state.graph.findForwardActiveLinkEdges(nextAbsHnIds, sampleIds)
      nextAbsNodes <- state.graph.getAbsForHnIds(forwardLinkEdges.keySet.map(_.trg))
      backwordThenEdges <- state.graph.findBackwardActiveThenEdges(nextAbsHnIds, sampleIds)
      newDag <- if nextAbsNodes.nonEmpty then traceActiveAbsNodes(dag, nextAbsNodes.keySet, state) else dag.pure
      updatedDag <- newDag.addAbstractLevel(nextAbsNodes.values, forwardLinkEdges.values, backwordThenEdges.keySet)
    yield updatedDag
