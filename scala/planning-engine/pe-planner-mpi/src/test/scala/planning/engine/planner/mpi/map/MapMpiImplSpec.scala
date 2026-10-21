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
| created: 2026-09-19 |||||||||||*/

package planning.engine.planner.mpi.map

import cats.effect.IO
import cats.effect.cps.*
import cats.effect.std.AtomicCell
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.common.values.io.{IoIndex, IoName, IoTime, IoValue}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.guardian.Guardian
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.io.{IoVars, Type, Variable}

class MapMpiImplSpec extends UnitSpecWithIOAndTestKit with AsyncIdiomaticMockito:

  private class CaseData extends Case:
    val guardianStub: Guardian = mock[Guardian]
    val managerStub: Manager = mock[Manager]
    val plannerStub: Planner = mock[Planner]

    val metadata: Metadata = Metadata(Name("test-map"), None)
    val inVar: Variable.Input = Variable.Input(IoName("testInput"), Type.Bool(Set(true, false)))
    val outVar: Variable.Output = Variable.Output(IoName("testOutput"), Type.Bool(Set(true, false)))
    val inVars: Set[Variable.Input] = Set(inVar)
    val outVars: Set[Variable.Output] = Set(outVar)
    val ioVars: IoVars = new IoVars(Map(inVar.name -> inVar), Map(outVar.name -> outVar))

    val conId: MnId.Con = MnId.Con(1L)
    val absId: MnId.Abs = MnId.Abs(2L)
    val absData: NodeData = NodeData.Abs(Some(HnName("test-abs-node")), None)
    val linkKey: MeKey = MeKey.Link(conId, absId)
    val sampleIds: Set[SampleId] = Set(SampleId(1L), SampleId(2L))

    guardianStub.initialize[IO](*, *) returns IO.pure((managerStub, plannerStub, None))

    val mapStateCell: AtomicCell[IO, Option[MapMpiImpl.MapState]] = AtomicCell[IO]
      .of(Option.empty[MapMpiImpl.MapState]).unsafeRunSync()

    val mapMpi: MapMpiImpl[IO] = new MapMpiImpl[IO](guardianStub, scheduler, mapStateCell)

  "MapMpiImpl.init(...)" should:
    "call Guardian.initialize with the given input/output variables" in
      newCase[CaseData]: (tn, data) =>
        import data.*
        async[IO]:
          mapMpi.init(metadata, inVars, outVars).logValue(tn).await
          guardianStub.initialize[IO](metadata, ioVars) was called

  "MapMpiImpl.reset(...)" should:
    "call Guardian.reset when the map network is initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      guardianStub.reset[IO]() returns IO.unit

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        mapMpi.reset().logValue(tn).await

        guardianStub.reset[IO]() was called
        succeed

  "MapMpiImpl.getIoVars" should:
    "return IO variables the map network was initialized with" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        val result = mapMpi.getIoVars.logValue(tn).await

        result mustBe ioVars

    "raise an error when the map network is not initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      mapMpi.getIoVars.logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map network not initialized"))

  "MapMpiImpl.addNode(...)" should:
    "call Manager.addNode with the given node data and return new node ID" in newCase[CaseData]: (tn, data) =>
      import data.*
      managerStub.addNode[IO](*) returns IO.pure(absId)

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        val result = mapMpi.addNode(absData).logValue(tn).await

        managerStub.addNode[IO](absData) was called
        result mustBe absId

    "raise an error when the map network is not initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      mapMpi.addNode(absData).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map network not initialized"))

  "MapMpiImpl.upsertNodesByName(...)" should:
    "call Manager.upsertNodesByName with the given node data and return node ID" in newCase[CaseData]: (tn, data) =>
      import data.*
      managerStub.upsertNodesByName[IO](*) returns IO.pure(absId)

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        val result = mapMpi.upsertNodesByName(absData).logValue(tn).await

        managerStub.upsertNodesByName[IO](absData) was called
        result mustBe absId

    "raise an error when the map network is not initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      mapMpi.upsertNodesByName(absData).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map network not initialized"))

  "MapMpiImpl.addEdge(...)" should:
    "call Manager.addEdge with the given edge key and sample IDs and return the key" in newCase[CaseData]: (tn, data) =>
      import data.*
      managerStub.addEdge[IO](*, *) returns IO.pure(linkKey)

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        val result = mapMpi.addEdge(linkKey, sampleIds).logValue(tn).await

        managerStub.addEdge[IO](linkKey, sampleIds) was called
        result mustBe linkKey

    "raise an error when the map network is not initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      mapMpi.addEdge(linkKey, sampleIds).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map network not initialized"))

  "MapMpiImpl.addSamples(...)" should:
    "call Manager.addManSamples with the given samples and nodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      val samples: Set[Sample.Man] = Set.empty
      val nodes: Map[MnId.Nim, NodeData] = Map.empty
      managerStub.addManSamples[IO](*, *) returns IO.pure(Map.empty[SampleId, Sample.Man])

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        val result = mapMpi.addSamples(samples, nodes).logValue(tn).await

        managerStub.addManSamples[IO](samples, nodes) was called
        result mustBe Map.empty

  "MapMpiImpl.syncPlannerTick(...)" should:
    "call Planner.tick with the given observation and return its action" in newCase[CaseData]: (tn, data) =>
      import data.*
      val observation = Observation(IoTime(1L), Set(IoValue(inVar.name, IoIndex(0))), utility = Some(0.5))
      val action = Action(Set(IoValue(outVar.name, IoIndex(1))))
      plannerStub.tick[IO](*) returns IO.pure(action)

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        val result = mapMpi.syncPlannerTick(observation).logValue(tn).await

        plannerStub.tick[IO](observation) was called
        result mustBe action

    "raise an error when the map network is not initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      mapMpi.syncPlannerTick(Observation(IoTime(1L), Set.empty, None)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Map network not initialized"))
