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
import planning.engine.planner.map.dcg.state.{DcgState, MapInfoState}
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.planner.map.dcg.edges.DcgEdgeData
import planning.engine.planner.map.dcg.edges.DcgEdgeData.EndIds
import planning.engine.planner.map.visualization.MapVisualizationLike

abstract class MapBaseLogic[F[_]: {Async, LoggerFactory}](
    visualization: MapVisualizationLike[F],
    mapInfoCell: AtomicCell[F, MapInfoState[F]],
    stateCell: AtomicCell[F, DcgState[F]]
):
  private[map] def getMapInfo: F[MapInfoState[F]] = mapInfoCell.get
  private[map] def setMapInfo(info: MapInfoState[F]): F[Unit] = mapInfoCell.set(info)
  private[map] def getMapState: F[DcgState[F]] = stateCell.get
  private[map] def setMapState(state: DcgState[F]): F[Unit] = stateCell.set(state)

  private[map] def modifyMapState[R](proc: DcgState[F] => F[(DcgState[F], R)]): F[R] =
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
        _ <- (state.allHnIds, samples.flatMap(_.allHnIds)).assertContainsAll("New samples contain unknown HnIds")
        allEdges = samples.flatMap(_.edges.toList)
        groupedEdges = allEdges.groupBy(e => (e.edgeType, EndIds(e.source.hnId, e.target.hnId))).toList
        dcgEdges <- groupedEdges.traverse((k, es) => DcgEdgeData(k._1, k._2, es))
        stateWithEdges <- state.mergeEdges(dcgEdges)
        stateWithSamples <- stateWithEdges.addSamples(samples.map(_.data))
      yield (stateWithSamples, samples.map(s => s.data.id -> s).toMap)
