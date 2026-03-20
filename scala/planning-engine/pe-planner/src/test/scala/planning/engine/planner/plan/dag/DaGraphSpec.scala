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
import planning.engine.planner.plan.test.data.DaGraphTestData

class DaGraphSpec extends UnitSpecWithData:

  private class CaseData extends Case with DaGraphTestData

  "DaGraph.linkEdges" should:
    "return link edges" in newCase[CaseData]: (tn, data) =>
      import data.{simpleDaGraph, dagEdgesLink}
      simpleDaGraph.linkEdges.pure[IO].logValue(tn).asserting(_.toSet mustBe dagEdgesLink.toSet)

  "DaGraph.thenEdges" should:
    "return then edges" in newCase[CaseData]: (tn, data) =>
      import data.{simpleDaGraph, dagEdgesThen}
      simpleDaGraph.thenEdges.pure[IO].logValue(tn).asserting(_.toSet mustBe dagEdgesThen.toSet)

  "DaGraph.makeSrcMap(...)" should:
    "make src map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleDaGraph.makeSrcMap[Link](dagEdgesLink) mustBe Map(
          snId1 -> Set(Link(snId1, snId3)),
          snId2 -> Set(Link(snId2, snId4)),
          snId3 -> Set(Link(snId3, snId6))
        )

        simpleDaGraph.makeSrcMap[Then](dagEdgesThen) mustBe Map(
          snId1 -> Set(Then(snId1, snId2)),
          snId3 -> Set(Then(snId3, snId4)),
          snId4 -> Set(Then(snId4, snId5))
        )

  "DaGraph.makeTrgLinkMap(...)" should:
    "make trg link map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDaGraph.makeTrgLinkMap(dagEdgesLink).pure[IO].logValue(tn)
        .asserting(_ mustBe Map(
          snId3 -> Set(Link(snId1, snId3)),
          snId4 -> Set(Link(snId2, snId4)),
          snId6 -> Set(Link(snId3, snId6))
        ))

  "DaGraph.makeTrgThenMap(...)" should:
    "make trg then map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDaGraph.makeTrgThenMap(dagEdgesThen).logValue(tn)
        .asserting(_ mustBe Map(snId5 -> Then(snId4, snId5), snId4 -> Then(snId3, snId4), snId2 -> Then(snId1, snId2)))

    "fail of planning DAG is not a forest" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidThenEdges = List(makeDagEdgeThen(snId1, snId5), makeDagEdgeThen(snId2, snId5))

      simpleDaGraph.makeTrgThenMap(invalidThenEdges).logValue(tn)
        .assertThrowsError(_.getMessage must include("Planning DAG need to be a forest"))

  "DaGraph.traceAbsDagLayers(...)" should:
    "trace simple DAG layers" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleDaGraph.traceAbsDagLayers(Set(snId1)).await mustBe List(
          Set(Link(snId1, snId3)),
          Set(Link(snId3, snId6))
        )

        simpleDaGraph.traceAbsDagLayers(Set(snId1, snId2)).await mustBe List(
          Set(Link(snId1, snId3), Link(snId2, snId4)),
          Set(Link(snId3, snId6))
        )

    "fail if cycle detected" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidLinkEdges = List(
        makeDagEdgeLink(snId1, snId3),
        makeDagEdgeLink(snId3, snId6),
        makeDagEdgeLink(snId6, snId3)
      )

      val invalidDaGraph = makeDaGraph(conDagNodes ++ absDagNodes, invalidLinkEdges ++ dagEdgesThen)

      invalidDaGraph.traceAbsDagLayers(Set(snId1)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Cycle detected on"))

    "fail if found LINK pointed on concrete node" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidLinkEdges = List(makeDagEdgeLink(snId1, snId3), makeDagEdgeLink(snId3, snId1))
      val invalidDaGraph = makeDaGraph(conDagNodes ++ absDagNodes, invalidLinkEdges ++ dagEdgesThen)

      invalidDaGraph.traceAbsDagLayers(Set(snId1)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Found LINK pointed on concrete node"))
