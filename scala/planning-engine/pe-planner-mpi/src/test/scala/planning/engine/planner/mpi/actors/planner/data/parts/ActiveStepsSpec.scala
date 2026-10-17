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
| created: 2026-10-18 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data.parts

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath

class ActiveStepsSpec extends UnitSpecWithData:
  private class CaseData extends Case:
    val root1: MnId.Con = MnId.Con(1L)
    val root2: MnId.Con = MnId.Con(2L)
    val unknownRoot: MnId.Con = MnId.Con(99L)

    val absId1: MnId.Abs = MnId.Abs(3L)
    val absId2: MnId.Abs = MnId.Abs(4L)

    val pu: PU = PU(p = 1.0, u = 0.0)

    def makePath(root: MnId.Con, absId: MnId.Abs, weight: HuangWeight): ActivePath =
      ActivePath(pathRoot = root, linkPath = List(absId), activeNodes = Map.empty, pu = pu, weight = weight)

    lazy val halves: List[HuangWeight] = HuangWeight.init.div[IO](2).unsafeRunSync()

    lazy val path1Half1: ActivePath = makePath(root1, absId1, halves.head)
    lazy val path1Half2: ActivePath = makePath(root1, absId2, halves(1))
    lazy val path2Full: ActivePath = makePath(root2, absId1, HuangWeight.init)

    lazy val steps: ActiveSteps = ActiveSteps(Set[MnId](root1, root2))

  "ActiveSteps.empty" should:
    "have empty weight accumulator and paths" in newCase[CaseData]: (_, _) =>
      async[IO]:
        ActiveSteps.empty.weightAcc mustBe Map.empty
        ActiveSteps.empty.paths mustBe Set.empty

  "ActiveSteps.apply(...)" should:
    "init weight accumulator with zero weight for each given root ID and empty paths" in newCase[CaseData]:
      (_, data) =>
        import data.*
        async[IO]:
          steps.weightAcc mustBe Map(root1 -> HuangWeight.zero, root2 -> HuangWeight.zero)
          steps.paths mustBe Set.empty

  "ActiveSteps.addPath(...)" should:
    "add path weight to the accumulator of its root and add path to paths" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path1Half1).logValue(tn)
        .asserting(_ mustBe ActiveSteps(
          weightAcc = Map(root1 -> halves.head, root2 -> HuangWeight.zero),
          paths = Set(path1Half1),
        ))

    "accumulate weights of several paths with the same root" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val withOne = steps.addPath[IO](path1Half1).await
        val withTwo = withOne.addPath[IO](path1Half2).logValue(tn).await

        withTwo.weightAcc mustBe Map(root1 -> HuangWeight.init, root2 -> HuangWeight.zero)
        withTwo.paths mustBe Set(path1Half1, path1Half2)

    "fail if path root is not in the weight accumulator" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](makePath(unknownRoot, absId1, HuangWeight.init)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("is not in the weight accumulator"))

    "fail if the same path is added twice" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path1Half1).flatMap(_.addPath[IO](path1Half1)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("already exists in the active paths"))

    "fail if accumulated weight overflows" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path2Full).flatMap(_.addPath[IO](makePath(root2, absId2, halves.head))).logValue(tn)
        .assertThrowsError[ArithmeticException](_.getMessage must include("overflow"))

  "ActiveSteps.isBuilt" should:
    "be true for empty active steps" in newCase[CaseData]: (_, _) =>
      IO.pure(ActiveSteps.empty.isBuilt).asserting(_ mustBe true)

    "be false when no paths added yet" in newCase[CaseData]: (_, data) =>
      import data.*
      IO.pure(steps.isBuilt).asserting(_ mustBe false)

    "be false when only part of roots have full weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path2Full).logValue(tn).asserting(_.isBuilt mustBe false)

    "be true when all roots have collected full weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path1Half1)
        .flatMap(_.addPath[IO](path1Half2))
        .flatMap(_.addPath[IO](path2Full))
        .logValue(tn)
        .asserting(_.isBuilt mustBe true)
