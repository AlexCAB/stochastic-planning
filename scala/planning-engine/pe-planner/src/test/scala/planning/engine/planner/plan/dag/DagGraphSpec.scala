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
| created: 2026-03-18 |||||||||||*/

package planning.engine.planner.plan.dag

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.plan.test.data.DagGraphTestData

class DagGraphSpec extends UnitSpecWithData:

  private class CaseData extends Case with DagGraphTestData

  "DagGraph.linkEdges" should:
    "return link edges" in newCase[CaseData]: (tn, data) =>
      import data.{simpleDagGraph, dagEdgesLink}
      simpleDagGraph.linkEdges.pure[IO].logValue(tn).asserting(_.toSet mustBe dagEdgesLink.toSet)

  "DagGraph.thenEdges" should:
    "return then edges" in newCase[CaseData]: (tn, data) =>
      import data.{simpleDagGraph, dagEdgesThen}
      simpleDagGraph.thenEdges.pure[IO].logValue(tn).asserting(_.toSet mustBe dagEdgesThen.toSet)

  "DagGraph.makeSrcMap(...)" should:
    "make src map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDagGraph.makeSrcMap(dagEdgesLink).pure[IO].logValue(tn)
        .asserting(_ mustBe Map(snId1 -> Set(snId3), snId2 -> Set(snId4)))

  "DagGraph.makeTrgLinkMap(...)" should:
    "make trg link map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDagGraph.makeTrgLinkMap(dagEdgesLink).pure[IO].logValue(tn)
        .asserting(_ mustBe Map(snId3 -> Set(snId1), snId4 -> Set(snId2)))

  "DagGraph.makeTrgThenMap(...)" should:
    "make trg then map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDagGraph.makeTrgThenMap(dagEdgesThen).logValue(tn)
        .asserting(_ mustBe Map(snId5 -> snId4, snId4 -> snId3, snId2 -> snId1))

    "fail of planning DAG is not a forest" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidThenEdges = List(makeDagEdgeThen(snId1, snId5), makeDagEdgeThen(snId2, snId5))

      simpleDagGraph.makeTrgThenMap(invalidThenEdges).logValue(tn)
        .assertThrowsError(_.getMessage must include("Planning DAG need to be a forest"))
