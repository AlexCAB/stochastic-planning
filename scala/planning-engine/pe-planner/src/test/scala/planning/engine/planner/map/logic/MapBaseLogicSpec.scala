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
import cats.syntax.all.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.map.test.data.SimpleMemStateTestData
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.state.DcgState
import planning.engine.planner.map.visualization.MapVisInLike

class MapBaseLogicSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case with SimpleMemStateTestData:
    val stateUpdatedStub = stubFunction[DcgState[IO], IO[Unit]]
    val visualizationStub = stub[MapVisInLike[IO]]

    val changedDcgState = initialDcgState.copy(ioValues = Map(testIoValue -> Set()))
    val dcgStateCell: AtomicCell[IO, DcgState[IO]] = AtomicCell[IO].of(initialDcgState).unsafeRunSync()

    val mapBaseLogic = new MapBaseLogic[IO](visualizationStub, dcgStateCell) {}

  "MapBaseLogic.getMapState" should:
    "get current map state" in newCase[CaseData]: (tn, data) =>
      data.mapBaseLogic.getMapState.logValue(tn).asserting(_ mustBe data.initialDcgState)

  "MapBaseLogic.setMapState(...)" should:
    "set new map state and call stateUpdated" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.mapBaseLogic.setMapState(data.changedDcgState).logValue(tn).await
        val currentState = data.mapBaseLogic.getMapState.logValue(tn).await
        currentState mustBe data.changedDcgState

  "MapBaseLogic.modifyMapState(...)" should:
    "modify map state and call stateUpdated" in newCase[CaseData]: (tn, data) =>
      data.visualizationStub.stateUpdated.when(data.changedDcgState).returning(IO.unit).once()

      async[IO]:
        val result = data.mapBaseLogic
          .modifyMapState: state =>
            state mustBe data.initialDcgState
            IO.pure((data.changedDcgState, 42))
          .logValue(tn).await

        result mustBe 42

    "MapBaseLogic.addNewSamplesToCache(...)" should:
      "add new samples to the map state" in newCase[CaseData]: (tn, data) =>
        data.visualizationStub.stateUpdated.when(*)
          .onCall: state =>
            for
              _ <- logInfo(tn, s"State updated called with $state")
              _ <- IO.delay(state.allHnIds mustBe data.allHnId)
              _ <- IO.delay(state.allSampleIds mustBe data.initSamples.map(_.data.id).toSet)
            yield ()
          .once()

        async[IO]:
          val result = data.mapBaseLogic.addNewSamplesToCache(IO.pure(data.initSamples)).logValue(tn).await
          val state = data.mapBaseLogic.getMapState.logValue(tn).await
          
          val dcgEdges = data
            .initSamples.flatMap(_.edges)
            .groupBy(e => DcgEdge.Key(e.edgeType, e.source.hnId, e.target.hnId))
            .toList
            .traverse((k, es) => DcgEdge[IO](k, es)).await.map(e => e.key -> e).toMap

          result mustBe data.initSamples.map(s => s.data.id -> s).toMap
          state.ioValues mustBe data.conNodes.map(n => n.ioValue -> Set(n.id)).toMap
          state.concreteNodes mustBe data.conDcgNodes.map(n => n.id -> n).toMap
          state.abstractNodes mustBe data.absDcgNodes.map(n => n.id -> n).toMap
          state.edges mustBe dcgEdges
          state.forwardLinks mustBe Map(data.hnId1 -> Set(data.hnId2), data.hnId2 -> Set(data.hnId1))
          state.backwardLinks mustBe Map(data.hnId2 -> Set(data.hnId1), data.hnId1 -> Set(data.hnId2))
          state.forwardThen mustBe Map.empty
          state.backwardThen mustBe Map.empty
          state.samplesData mustBe data.initSamples.map(s => s.data.id -> s.data).toMap

      "not add samples with unknown HnIds" in newCase[CaseData]: (tn, data) =>
        data.mapBaseLogic
          .addNewSamplesToCache(IO.pure(List(data.makeSample(SampleId(2001), HnId(-1), HnId(-2)))))
          .logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must startWith("New samples contain unknown HnIds"))
