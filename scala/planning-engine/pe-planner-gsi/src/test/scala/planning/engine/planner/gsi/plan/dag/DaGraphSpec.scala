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

package planning.engine.planner.gsi.plan.dag

import cats.effect.IO
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.PeKey.{Link, Then}
import planning.engine.common.graph.trees.PlanTree
import planning.engine.common.graph.trees.PlanTree.Vertex
import planning.engine.common.values.node.PnId
import planning.engine.planner.gsi.plan.dag.nodes.DagNode
import planning.engine.planner.gsi.plan.test.data.DaGraphTestData

class DaGraphSpec extends UnitSpecWithData:

  private class CaseData extends Case with DaGraphTestData:
    lazy val pnId1PlanTree = PlanTree(Vertex(List(Vertex(List.empty, pnId2)), pnId1))
    lazy val pnId3PlanTree = PlanTree(Vertex(List(Vertex(List(Vertex(List.empty, pnId5)), pnId4)), pnId3))

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
          pnId1 -> Set(Link(pnId1, pnId3)),
          pnId2 -> Set(Link(pnId2, pnId4)),
          pnId3 -> Set(Link(pnId3, pnId6))
        )

        simpleDaGraph.makeSrcMap[Then](dagEdgesThen) mustBe Map(
          pnId1 -> Set(Then(pnId1, pnId2)),
          pnId3 -> Set(Then(pnId3, pnId4)),
          pnId4 -> Set(Then(pnId4, pnId5))
        )

  "DaGraph.makeTrgLinkMap(...)" should:
    "make trg link map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDaGraph.makeTrgLinkMap(dagEdgesLink).pure[IO].logValue(tn)
        .asserting(_ mustBe Map(
          pnId3 -> Set(Link(pnId1, pnId3)),
          pnId4 -> Set(Link(pnId2, pnId4)),
          pnId6 -> Set(Link(pnId3, pnId6))
        ))

  "DaGraph.makeTrgThenMap(...)" should:
    "make trg then map for given edges" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleDaGraph.makeTrgThenMap(dagEdgesThen).logValue(tn)
        .asserting(_ mustBe Map(pnId5 -> Then(pnId4, pnId5), pnId4 -> Then(pnId3, pnId4), pnId2 -> Then(pnId1, pnId2)))

    "fail of planning DAG is not a forest" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidThenEdges = List(makeDagEdgeThen(pnId1, pnId5), makeDagEdgeThen(pnId2, pnId5))

      simpleDaGraph.makeTrgThenMap(invalidThenEdges).logValue(tn)
        .assertThrowsError(_.getMessage must include("Planning DAG need to be a forest"))

  "DaGraph.srcLinkMap" should:
    "return src link map" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleDaGraph.srcLinkMap.pure[IO].logValue(tn)
        .asserting(_ mustBe Map(
          pnId1 -> Set(Link(pnId1, pnId3)),
          pnId2 -> Set(Link(pnId2, pnId4)),
          pnId3 -> Set(Link(pnId3, pnId6))
        ))

  "DaGraph.trgLinkMap" should:
    "return trg link map" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleDaGraph.trgLinkMap.pure[IO].logValue(tn)
        .asserting(_ mustBe Map(
          pnId3 -> Set(Link(pnId1, pnId3)),
          pnId4 -> Set(Link(pnId2, pnId4)),
          pnId6 -> Set(Link(pnId3, pnId6))
        ))

  "DaGraph.srcThenMap" should:
    "return src then map" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleDaGraph.srcThenMap.pure[IO].logValue(tn)
        .asserting(_ mustBe Map(
          pnId1 -> Set(Then(pnId1, pnId2)),
          pnId3 -> Set(Then(pnId3, pnId4)),
          pnId4 -> Set(Then(pnId4, pnId5))
        ))

  "DaGraph.trgThenMap" should:
    "return trg then map" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleDaGraph.trgThenMap.logValue(tn)
        .asserting(_ mustBe Map(
          pnId5 -> Then(pnId4, pnId5),
          pnId4 -> Then(pnId3, pnId4),
          pnId2 -> Then(pnId1, pnId2)
        ))

  "DaGraph.thenRoots" should:
    "return then roots" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleDaGraph.thenRoots.logValue(tn).asserting(_ mustBe Set(pnId1, pnId3))

  "DaGraph.traceAbsDagLayers(...)" should:
    "trace simple DAG layers" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleDaGraph.traceAbsDagLayers(Set(pnId1)).await mustBe List(
          Set(Link(pnId1, pnId3)),
          Set(Link(pnId3, pnId6))
        )

        simpleDaGraph.traceAbsDagLayers(Set(pnId1, pnId2)).await mustBe List(
          Set(Link(pnId1, pnId3), Link(pnId2, pnId4)),
          Set(Link(pnId3, pnId6))
        )

    "fail if cycle detected" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidLinkEdges = List(
        makeDagEdgeLink(pnId1, pnId3),
        makeDagEdgeLink(pnId3, pnId6),
        makeDagEdgeLink(pnId6, pnId3)
      )

      val invalidDaGraph = makeDaGraph(allConDagNodes ++ allAbsDagNodes, invalidLinkEdges ++ dagEdgesThen)

      invalidDaGraph.traceAbsDagLayers(Set(pnId1)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Cycle detected on"))

    "fail if found LINK pointed on concrete node" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidLinkEdges = List(makeDagEdgeLink(pnId1, pnId3), makeDagEdgeLink(pnId3, pnId1))
      val invalidDaGraph = makeDaGraph(allConDagNodes ++ allAbsDagNodes, invalidLinkEdges ++ dagEdgesThen)

      invalidDaGraph.traceAbsDagLayers(Set(pnId1)).logValue(tn)
        .assertThrowsError(_.getMessage must include("Found LINK pointed on concrete node"))

  "DaGraph.tracePlanTree(...)" should:
    "trace simple plan tree" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val planTree = simpleDaGraph.tracePlanTree(pnId3).await
        logInfo(tn, s"Traced plan tree:\n$planTree").await
        planTree mustBe pnId3PlanTree

  "DaGraph.tracePlanForest(...)" should:
    "trace simple plan forest" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val planForest = simpleDaGraph.tracePlanForest(Set(pnId1, pnId3)).await
        logInfo(tn, s"Traced plan forest:\n${planForest.mkString("\n")}").await
        planForest.toSet mustBe Set(pnId1PlanTree, pnId3PlanTree)

  "DaGraph.empty" should:
    "return empty DaGraph" in newCase[CaseData]: (tn, data) =>
      DaGraph.empty[IO].pure[IO].logValue(tn).asserting(_ mustBe new DaGraph[IO](Map.empty, Map.empty))

  "DaGraph.apply(...)" should:
    "create DaGraph if nodes and edges are valid" in newCase[CaseData]: (tn, data) =>
      import data.*
      DaGraph[IO](allDagNodes.map(n => n.id -> n).toMap, allDagEdges.map(e => e.key -> e).toMap)
        .logValue(tn).asserting(_ mustBe simpleDaGraph)

    "fail if nodes map keys and values IDs mismatch" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidNodes: Map[PnId, DagNode[IO]] = Map(makeConPnId(999L) -> allConDagNodes.head)

      DaGraph[IO](invalidNodes, allDagEdges.map(e => e.key -> e).toMap)
        .logValue(tn).assertThrowsError(_.getMessage must include("Nodes map keys and values IDs mismatch"))

    "fail if edge refers to unknown MnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      val invalidEdges = List(makeDagEdgeLink(makeConPnId(999L), makeAbsPnId(998L)))

      DaGraph[IO](allDagNodes.map(n => n.id -> n).toMap, invalidEdges.map(e => e.key -> e).toMap)
        .logValue(tn).assertThrowsError(_.getMessage must include("Edge refers to unknown PnId"))
