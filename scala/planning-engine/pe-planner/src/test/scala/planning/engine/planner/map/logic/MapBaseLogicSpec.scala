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

package planning.engine.planner.map.logic

import cats.effect.IO
import cats.effect.std.AtomicCell
import cats.effect.cps.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.dcg.samples.DcgSample
import planning.engine.planner.map.state.{MapGraphState, MapInfoState}
import planning.engine.planner.map.test.data.DcgStatesTestData
import planning.engine.planner.map.visualization.MapVisualizationLike

class MapBaseLogicSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with DcgStatesTestData:
    lazy val stateUpdatedStub = stubFunction[MapGraphState[IO], IO[Unit]]
    lazy val visualizationStub = stub[MapVisualizationLike[IO]]

    lazy val changedDcgState = initDcgState.copy(ioValues = Map(testIoValue -> Set()))

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
      data.visualizationStub.stateUpdated.when(data.initMapInfoState, data.changedDcgState).returning(IO.unit).once()

      async[IO]:
        val result = data.mapBaseLogic
          .modifyMapState: state =>
            state mustBe data.initDcgState
            IO.pure((data.changedDcgState, 42))
          .logValue(tn).await

        result mustBe 42

  "MapBaseLogic.addNewSamplesToState(...)" should:
    "add new samples to the map state" in newCase[CaseData]: (tn, data) =>
      import data.*

      visualizationStub.stateUpdated.when(*, *)
        .onCall: (info, state) =>
          for
            _ <- logInfo(tn, s"State updated called with $state")
            _ <- IO.delay(info mustBe initMapInfoState)
            _ <- IO.delay(state.graph.samples must contain key simpleSampleId)
          yield ()
        .once()

      def newSamples(state: MapGraphState[IO]): IO[List[DcgSample.Add[IO]]] =
        state mustBe initDcgState
        IO.pure(List(simpleSampleAdd))

      async[IO]:
        val result: Map[SampleId, DcgSample[IO]] = mapBaseLogic.addNewSamplesToState(newSamples).logValue(tn).await
        val state = mapBaseLogic.getMapState.logValue(tn).await

        result mustBe Map(simpleSampleId -> simpleSampleAdd.sample)
        state.graph.samples must contain key simpleSampleId
