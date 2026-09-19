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
| created: 2025-12-21 |||||||||||*/

package planning.engine.planner.gsi.map.logic

import cats.effect.IO
import cats.effect.cps.*
import cats.effect.std.AtomicCell
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.gsi.map.dcg.samples.DcgSample
import planning.engine.planner.gsi.map.state.{MapGraphState, MapInfoState}
import planning.engine.planner.gsi.map.test.data.DcgStatesTestData
import planning.engine.planner.gsi.map.visualization.MapVisualizationLike

class MapBaseLogicSpec extends UnitSpecWithData with AsyncIdiomaticMockito:

  private class CaseData extends Case with DcgStatesTestData:
    lazy val visualizationStub: MapVisualizationLike[IO] = mock[MapVisualizationLike[IO]]

    lazy val changedDcgState = initDcgState.copy(ioValues = makeIoValueMap(testIoValue -> Set()))

    lazy val mapInfoCell: AtomicCell[IO, MapInfoState[IO]] = AtomicCell[IO].of(initMapInfoState).unsafeRunSync()
    lazy val dcgStateCell: AtomicCell[IO, MapGraphState[IO]] = AtomicCell[IO].of(initDcgState).unsafeRunSync()

    lazy val mapBaseLogic = new MapBaseLogic[IO](visualizationStub, mapInfoCell, dcgStateCell) {}

  "MapBaseLogic.getMapState" should:
    "get current map state" in newCase[CaseData]: (tn, data) =>
      data.mapBaseLogic.getMapState.logValue(tn).asserting(_ mustBe data.initDcgState)

  "MapBaseLogic.setMapState(...)" should:
    "set new map state and call stateUpdated" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapBaseLogic.setMapState(data.changedDcgState).logValue(tn).await
        val currentState = data.mapBaseLogic.getMapState.logValue(tn).await
        currentState mustBe data.changedDcgState

  "MapBaseLogic.modifyMapState(...)" should:
    "modify map state and call stateUpdated" in newCase[CaseData]: (tn, data) =>
      data.visualizationStub.stateUpdated(data.initMapInfoState, data.changedDcgState) returns IO.unit

      async[IO]:
        val result = data.mapBaseLogic
          .modifyMapState: state =>
            state mustBe data.initDcgState
            IO.pure((data.changedDcgState, 42))
          .logValue(tn).await

        data.visualizationStub.stateUpdated(data.initMapInfoState, data.changedDcgState) was called
        result mustBe 42

  "MapBaseLogic.addNewSamplesToState(...)" should:
    "add new samples to the map state" in newCase[CaseData]: (tn, data) =>
      import data.*

      def hasSimpleSample(state: MapGraphState[IO]): Boolean = state.graph.samples.contains(simpleSampleId)

      visualizationStub.stateUpdated(initMapInfoState, argThat((s: MapGraphState[IO]) => hasSimpleSample(s))) returns
        IO.unit

      def newSamples(state: MapGraphState[IO]): IO[List[DcgSample.Add[IO]]] =
        state mustBe initDcgState
        IO.pure(List(simpleSampleAdd))

      async[IO]:
        val result: Map[SampleId, DcgSample[IO]] = mapBaseLogic.addNewSamplesToState(newSamples).logValue(tn).await
        val state = mapBaseLogic.getMapState.logValue(tn).await

        visualizationStub.stateUpdated(
          initMapInfoState,
          argThat((s: MapGraphState[IO]) => hasSimpleSample(s)),
        ) was called
        result mustBe Map(simpleSampleId -> simpleSampleAdd.sample)
        state.graph.samples must contain key simpleSampleId
