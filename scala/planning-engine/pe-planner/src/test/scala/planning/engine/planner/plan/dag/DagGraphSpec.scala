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
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.PeKey.{Link, Then}
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
      async[IO]:
        simpleDagGraph.makeSrcMap[Link](dagEdgesLink)  mustBe Map(
          snId1 -> Set(Link(snId1, snId3)),
          snId2 -> Set(Link(snId2, snId4))
        )

        simpleDagGraph.makeSrcMap[Then](dagEdgesThen) mustBe Map(
          snId1 -> Set(Then(snId1, snId2)),
          snId3 -> Set(Then(snId3, snId4)),
          snId4 -> Set(Then(snId4, snId5))
        )

  "DagGraph.makeTrgLinkMap(...)" should:
    "make trg link map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDagGraph.makeTrgLinkMap(dagEdgesLink).pure[IO].logValue(tn)
        .asserting(_ mustBe Map(snId3 -> Set(Link(snId1, snId3)), snId4 -> Set(Link(snId2, snId4))))

  "DagGraph.makeTrgThenMap(...)" should:
    "make trg then map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDagGraph.makeTrgThenMap(dagEdgesThen).logValue(tn)
        .asserting(_ mustBe Map(snId5 -> Then(snId4, snId5), snId4 -> Then(snId3, snId4), snId2 -> Then(snId1, snId2)))

    "fail of planning DAG is not a forest" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidThenEdges = List(makeDagEdgeThen(snId1, snId5), makeDagEdgeThen(snId2, snId5))

      simpleDagGraph.makeTrgThenMap(invalidThenEdges).logValue(tn)
        .assertThrowsError(_.getMessage must include("Planning DAG need to be a forest"))
