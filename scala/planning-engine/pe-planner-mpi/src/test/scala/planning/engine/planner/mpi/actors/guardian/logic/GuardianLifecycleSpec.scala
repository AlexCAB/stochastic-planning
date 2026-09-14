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
| created: 2026-09-14 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian.logic

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.guardian.WithTestGuardian
import planning.engine.planner.mpi.actors.visualizer.WithTestVisualizer
import planning.engine.planner.mpi.model.io.Variable

class GuardianLifecycleSpec extends UnitSpecWithIOAndTestKit with WithTestGuardian with WithTestVisualizer:
  private class CaseData extends Case with WithGuardian with WithVisualizer

  "Guardian.initialize" should:
    "return newly created Manager, Planner and Visualizer actors when not yet initialized" in
      newCase[CaseData]: (tn, data) =>
        import data.*
        async[IO]:
          val (manager, planner, visualizer) = guardian.api
            .initialize[IO](Set.empty[Variable.Input], Set.empty[Variable.Output], Some(visualization))
            .logValue(tn).await

          manager must not be null
          planner must not be null
          visualizer mustBe defined

    "terminate the Guardian actor when called again without a reset" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val probe = testKit.createTestProbe()

        guardian.api.initialize[IO](Set.empty, Set.empty, None).logValue(tn).await
        guardian.api.initialize[IO](Set.empty, Set.empty, None).logValue(tn).attempt.await

        probe.expectTerminated(guardian.ref)
        succeed

  "Guardian.reset" should:
    "complete successfully when the Guardian was initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        guardian.api.initialize[IO](Set.empty, Set.empty, None).logValue(tn).await
        guardian.api.reset[IO]().logValue(tn).await
        succeed

    "complete successfully when the Guardian was not initialized" in newCase[CaseData]: (tn, data) =>
      import data.*
      guardian.api.reset[IO]().logValue(tn).asserting(_ => succeed)
