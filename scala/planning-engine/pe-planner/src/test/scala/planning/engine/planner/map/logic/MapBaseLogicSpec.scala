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
import planning.engine.planner.map.MapTestData
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.state.DcgState

class MapBaseLogicSpec extends UnitSpecWithData with AsyncMockFactory with MapTestData:

  private class CaseData extends Case:
    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)

    lazy val absNodes = List(hnId1, hnId2).map(id => makeAbstractDcgNode(id = id))
    lazy val conNodes = List(hnId3).map(id => makeConcreteDcgNode(id = id))

    lazy val sampleId1 = SampleId(1001)
    lazy val sampleId2 = SampleId(1002)

    lazy val samples = List(
      makeSample(sampleId1, hnId1, hnId2),
      makeSample(sampleId2, hnId2, hnId3)
    )

    val initialDcgState: DcgState[IO] = DcgState.init()
      .addAbstractNodes(absNodes)
      .flatMap(_.addConcreteNodes(conNodes))
      .unsafeRunSync()

    val stateUpdatedStub = stubFunction[DcgState[IO], IO[Unit]]
    val changedDcgState = initialDcgState.copy(ioValues = Map(testIoValue -> Set()))
    val dcgState = AtomicCell[IO].of(initialDcgState).unsafeRunSync()

    val mapBaseLogic = new MapBaseLogic[IO](dcgState):
      override private[map] def stateUpdated(state: DcgState[IO]): IO[Unit] = stateUpdatedStub(state)

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
      data.stateUpdatedStub.when(data.changedDcgState).returning(IO.unit).once()

      async[IO]:
        val result = data.mapBaseLogic
          .modifyMapState: state =>
            state mustBe data.initialDcgState
            IO.pure((data.changedDcgState, 42))
          .logValue(tn).await

        result mustBe 42

    "MapBaseLogic.addNewSamplesToCache(...)" should:
      "add new samples to the map state" in newCase[CaseData]: (tn, data) =>
        data.stateUpdatedStub.when(*)
          .onCall: state =>
            for
              _ <- logInfo(tn, s"State updated called with $state")
              _ = state.allHnIds mustBe Set(data.hnId1, data.hnId2, data.hnId3)
              _ = state.allSampleIds mustBe data.samples.map(_.data.id).toSet
            yield ()
          .once()

        async[IO]:
          val result = data.mapBaseLogic.addNewSamplesToCache(IO.pure(data.samples)).logValue(tn).await
          val state = data.mapBaseLogic.getMapState.logValue(tn).await
          val dcgEdges = data.samples.flatMap(_.edges).traverse(e => DcgEdge[IO](e)).await.map(e => e.key -> e).toMap

          result mustBe data.samples.map(s => s.data.id -> s).toMap
          state.ioValues mustBe data.conNodes.map(n => n.ioValue -> Set(n.id)).toMap
          state.concreteNodes mustBe data.conNodes.map(n => n.id -> n).toMap
          state.abstractNodes mustBe data.absNodes.map(n => n.id -> n).toMap
          state.edges mustBe dcgEdges
          state.forwardLinks mustBe Map(data.hnId1 -> Set(data.hnId2), data.hnId2 -> Set(data.hnId3))
          state.backwardLinks mustBe Map(data.hnId2 -> Set(data.hnId1), data.hnId3 -> Set(data.hnId2))
          state.forwardThen mustBe Map.empty
          state.backwardThen mustBe Map.empty
          state.samplesData mustBe data.samples.map(s => s.data.id -> s.data).toMap

      "not add samples with unknown HnIds" in newCase[CaseData]: (tn, data) =>
        data.mapBaseLogic
          .addNewSamplesToCache(IO.pure(List(makeSample(SampleId(2001), HnId(-1), HnId(-2)))))
          .logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must startWith("New samples contain unknown HnIds"))
