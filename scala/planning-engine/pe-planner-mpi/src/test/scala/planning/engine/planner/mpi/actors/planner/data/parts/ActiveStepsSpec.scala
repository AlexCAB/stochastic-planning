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
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath.ActiveNode
import planning.engine.planner.mpi.test.data.NodeTestData

class ActiveStepsSpec extends UnitSpecWithData with NodeTestData:
  private class CaseData extends Case with WithMapNode:
    val root1Id: MnId.Con = MnId.Con(1L)
    val root2Id: MnId.Con = MnId.Con(2L)
    val absId1: MnId.Abs = MnId.Abs(3L)
    val absId2: MnId.Abs = MnId.Abs(4L)

    val root1: Node.Con = makeConNodeStub(root1Id, inVarName, IoIndex(0))
    val root2: Node.Con = makeConNodeStub(root2Id, inVarName, IoIndex(1))
    val unknownRoot: Node.Con = makeConNodeStub(MnId.Con(99L), inVarName, IoIndex(2))
    val abs1: Node.Abs = makeAbsNodeStub(absId1)
    val abs2: Node.Abs = makeAbsNodeStub(absId2)

    val conPu: PU = PU(p = 1.0, u = 0.0)
    val absPu: PU = PU(p = 0.5, u = 2.0)

    def makePath(root: Node.Con, abs: Node.Abs, weight: HuangWeight): ActivePath = ActivePath(
      root = root.mnId,
      path = List(abs.mnId),
      activeNodes = Map(
        root.mnId -> ActiveNode(root, conPu, Set.empty),
        abs.mnId -> ActiveNode(abs, absPu, Set.empty),
      ),
      weight = weight,
    )

    lazy val halves: List[HuangWeight] = HuangWeight.init.div[IO](2).unsafeRunSync()

    lazy val path1Half1: ActivePath = makePath(root1, abs1, halves.head)
    lazy val path1Half2: ActivePath = makePath(root1, abs2, halves(1))
    lazy val path2Full: ActivePath = makePath(root2, abs1, HuangWeight.init)

    lazy val steps: ActiveSteps = ActiveSteps(Set[MnId](root1Id, root2Id))

    lazy val builtSteps: IO[ActiveSteps] = steps.addPath[IO](path1Half1)
      .flatMap(_.addPath[IO](path1Half2))
      .flatMap(_.addPath[IO](path2Full))

  "ActiveSteps.empty" should:
    "have empty weight accumulator and paths" in newCase[CaseData]: (_, _) =>
      async[IO]:
        ActiveSteps.empty.weightAcc mustBe Map.empty
        ActiveSteps.empty.paths mustBe Set.empty

  "ActiveSteps.apply(...)" should:
    "init weight accumulator with zero weight for each given root ID and empty" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        steps.weightAcc mustBe Map(root1Id -> HuangWeight.zero, root2Id -> HuangWeight.zero)
        steps.paths mustBe Set.empty

  "ActiveSteps.addPath(...)" should:
    "add path weight to the accumulator of its root and add path to paths" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path1Half1).logValue(tn)
        .asserting(_ mustBe ActiveSteps(
          weightAcc = Map(root1Id -> halves.head, root2Id -> HuangWeight.zero),
          paths = Set(path1Half1),
        ))

    "accumulate weights of several paths with the same root" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val withOne = steps.addPath[IO](path1Half1).await
        val withTwo = withOne.addPath[IO](path1Half2).logValue(tn).await

        withTwo.weightAcc mustBe Map(root1Id -> HuangWeight.init, root2Id -> HuangWeight.zero)
        withTwo.paths mustBe Set(path1Half1, path1Half2)

    "fail if path root is not in the weight accumulator" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](makePath(unknownRoot, abs1, HuangWeight.init)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("is not in the weight accumulator"))

    "fail if the same path is added twice" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path1Half1).flatMap(_.addPath[IO](path1Half1)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("already exists in the active paths"))

    "fail if accumulated weight overflows" in newCase[CaseData]: (tn, data) =>
      import data.*
      steps.addPath[IO](path2Full).flatMap(_.addPath[IO](makePath(root2, abs2, halves.head))).logValue(tn)
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
      builtSteps.logValue(tn).asserting(_.isBuilt mustBe true)

  "ActiveSteps.nodes" should:
    "be empty when no paths added yet" in newCase[CaseData]: (_, data) =>
      import data.*
      IO.pure(steps.nodes).asserting(_ mustBe Map.empty)

    "collect PU of active nodes from all paths" in newCase[CaseData]: (tn, data) =>
      import data.*
      builtSteps.logValue(tn)
        .asserting(_.nodes mustBe Map(root1Id -> conPu, root2Id -> conPu, absId1 -> absPu, absId2 -> absPu))

  "ActiveSteps.roots" should:
    "be empty when no paths added yet" in newCase[CaseData]: (_, data) =>
      import data.*
      IO.pure(steps.roots).asserting(_ mustBe Set.empty)

    "collect distinct roots of all paths" in newCase[CaseData]: (tn, data) =>
      import data.*
      builtSteps.logValue(tn).asserting(_.roots mustBe Set(root1Id, root2Id))

  "ActiveSteps.edges" should:
    "be empty when no paths added yet" in newCase[CaseData]: (_, data) =>
      import data.*
      IO.pure(steps.edges).asserting(_ mustBe Set.empty)

    "collect distinct edges of all paths" in newCase[CaseData]: (tn, data) =>
      import data.*
      builtSteps.logValue(tn).asserting(_.edges mustBe Set(root1Id -> absId1, root1Id -> absId2, root2Id -> absId1))
