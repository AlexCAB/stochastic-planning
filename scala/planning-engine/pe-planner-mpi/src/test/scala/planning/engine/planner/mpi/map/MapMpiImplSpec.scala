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
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.guardian.Guardian
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.io.{Type, Variable}
import planning.engine.common.values.text.Name

class MapMpiImplSpec extends UnitSpecWithIOAndTestKit with AsyncIdiomaticMockito:

  private class CaseData extends Case:
    val guardianStub: Guardian = mock[Guardian]
    val managerStub: Manager = mock[Manager]
    val plannerStub: Planner = mock[Planner]
    val visualizationStub: Visualization = mock[Visualization]

    val metadata: Metadata = Metadata(Name("test-map"), None)
    val inVar: Variable.Input = Variable.Input(IoName("testInput"), Type.Bool(Set(true, false)))
    val outVar: Variable.Output = Variable.Output(IoName("testOutput"), Type.Bool(Set(true, false)))
    val inVars: Set[Variable.Input] = Set(inVar)
    val outVars: Set[Variable.Output] = Set(outVar)

    guardianStub.initialize[IO](*, *, *) returns IO.pure((managerStub, plannerStub, None))

    val mapStateCell: AtomicCell[IO, Option[MapMpiImpl.MapState]] = AtomicCell[IO]
      .of(Option.empty[MapMpiImpl.MapState]).unsafeRunSync()

    val mapMpi: MapMpiImpl[IO] = new MapMpiImpl[IO](Some(visualizationStub), guardianStub, scheduler, mapStateCell)

  "MapMpiImpl.init(...)" should:
    "call Guardian.initialize with the given input/output variables and visualization" in
      newCase[CaseData]: (tn, data) =>
        import data.*
        mapMpi.init(metadata, inVars, outVars).logValue(tn).asserting: _ =>
          guardianStub.initialize[IO](inVars, outVars, Some(visualizationStub)) was called
          succeed

  "MapMpiImpl.reset(...)" should:
    "call Guardian.reset when the map network is initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      guardianStub.reset[IO]() returns IO.unit

      async[IO]:
        mapMpi.init(metadata, inVars, outVars).logValue(tn).await
        mapMpi.reset().logValue(tn).await

        guardianStub.reset[IO]() was called
        succeed

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
