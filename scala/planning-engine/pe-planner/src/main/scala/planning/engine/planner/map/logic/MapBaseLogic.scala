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
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}
import planning.engine.planner.map.visualization.MapVisualizationLike

// Base/common logic for MapInMem and MapCache implementations,
// providing common state management and operations on the map graph state.
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

  private[map] def addNewSamplesToState(
      newSamples: MapGraphState[F] => F[Iterable[DcgSample.Add[F]]]
  ): F[Map[SampleId, DcgSample[F]]] = modifyMapState: state =>
    for
      samples <- newSamples(state)
      newSate <- state.addSamples(samples)
    yield (newSate, samples.map(s => s.sample.data.id -> s.sample).toMap)
