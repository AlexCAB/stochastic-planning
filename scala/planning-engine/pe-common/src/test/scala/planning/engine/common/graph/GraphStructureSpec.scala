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
| created: 2026-01-27 |||||||||||*/

package planning.engine.common.graph

import cats.effect.IO
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.graph.edges.EdgeKey

import planning.engine.common.values.node.MnId

class GraphStructureSpec extends UnitSpecWithData:
  import EdgeKey.{End, Link, Then}
  import MnId.{Abs, Con}

  private class CaseData extends Case with GraphStructureTestData
  
  "GraphStructure.mnIds" should:
    "return all MnIds in the graph" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleGraph.mnIds.pure[IO].logValue(tn).asserting(_ mustBe Set(c1, c2, c3, a4, a5, a6))

  "GraphStructure.conMnId" should:
    "return all concrete MnIds in the graph" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleGraph.conMnId.pure[IO].logValue(tn).asserting(_ mustBe Set(c1, c2, c3))

  "GraphStructure.absMnId" should:
    "return all abstract MnIds in the graph" in newCase[CaseData]: (tn, data) =>
      import data.*
      simpleGraph.absMnId.pure[IO].logValue(tn).asserting(_ mustBe Set(a4, a5, a6))

  "GraphStructure.filterByEndType(...)" should:
    "filter ends by given type" in newCase[CaseData]: (tn, data) =>
      import data.{c1, c2, c3, simpleGraph}
      async[IO]:
        val inMap: Map[MnId, Set[EdgeKey.End]] = Map(
          c1 -> Set(Link.End(c1), Then.End(c1)),
          c2 -> Set(Link.End(c2)),
          c3 -> Set(Then.End(c3))
        )

        simpleGraph.filterByEndType[Link.End](inMap) mustBe Map(c1 -> Set(Link.End(c1)), c2 -> Set(Link.End(c2)))
        simpleGraph.filterByEndType[Then.End](inMap) mustBe Map(c1 -> Set(Then.End(c1)), c3 -> Set(Then.End(c3)))

  "GraphStructure.srcLinkMap" should:
    "return source to target Link ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleGraph.srcLinkMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        c1 -> Set(Link.End(a4), Link.End(a6)),
        a4 -> Set(Link.End(a5))
      ))

  "GraphStructure.srcThenMap" should:
    "return source to target Then ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{c1, c2, c3, simpleGraph}
      simpleGraph.srcThenMap.pure[IO].logValue(tn).asserting(_ mustBe Map(c1 -> Set(Then.End(c2), Then.End(c3))))

  "GraphStructure.trgLinkMap" should:
    "return target to source Link ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleGraph.trgLinkMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        a4 -> Set(Link.End(c1)),
        a5 -> Set(Link.End(a4)),
        a6 -> Set(Link.End(c1))
      ))

  "GraphStructure.trgThenMap" should:
    "return target to source Then ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{c1, c2, c3, simpleGraph}

      simpleGraph.trgThenMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        c2 -> Set(Then.End(c1)),
        c3 -> Set(Then.End(c1))
      ))

  "GraphStructure.neighbours" should:
    "return neighbours mapping" in newCase[CaseData]: (tn, data) =>
      import data.*

      simpleGraph.neighbours.pure[IO].logValue(tn).asserting(_ mustBe Map(
        c1 -> Set(c2, c3, a4, a6),
        c2 -> Set(c1),
        c3 -> Set(c1),
        a4 -> Set(c1, a5),
        a5 -> Set(a4),
        a6 -> Set(c1)
      ))

  "GraphStructure.findConnected" should:
    "return set of connected MnIds for given mnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        conGraph.findConnected(c1, Set.empty) mustBe Set(c1, a4, a5)
        nonConGraph.findConnected(c1, Set.empty) mustBe Set(c1, a4)

  "GraphStructure.isConnected" should:
    "return whether the graph is connected" in newCase[CaseData]: (tn, data) =>
      import data.{conGraph, nonConGraph}
      async[IO]:
        conGraph.isConnected mustBe true
        nonConGraph.isConnected mustBe false

  "GraphStructure.add" should:
    "add new edges to the graph" in newCase[CaseData]: (tn, data) =>
      import data.{c2, c3, a4, simpleGraph}
      async[IO]:
        val newEdges = Set(Link(c2, a4), Link(c3, a4))
        val newGraph: GraphStructure[IO] = simpleGraph.add(newEdges).await
        newGraph.keys mustBe (simpleGraph.keys ++ newEdges)
  
  "GraphStructure.linkRoots" should:
    "return link root mnIds" in newCase[CaseData]: (tn, data) =>
      import data.{c1, c2, c3, simpleGraph, conGraph, nonConGraph, cycleGraph, complexGraph}
      async[IO]:
        simpleGraph.linkRoots mustBe Set(c1)
        conGraph.linkRoots mustBe Set(c1)
        nonConGraph.linkRoots mustBe Set(c1, c2)
        cycleGraph.linkRoots mustBe Set()
        complexGraph.linkRoots mustBe Set(c1, c2, c3)

  "GraphStructure.thenRoots" should:
    "return then root mnIds" in newCase[CaseData]: (tn, data) =>
      import data.{c1, a4, simpleGraph, conGraph, nonConGraph, cycleGraph, complexGraph}
      async[IO]:
        simpleGraph.thenRoots mustBe Set(c1)
        conGraph.thenRoots mustBe Set()
        nonConGraph.thenRoots mustBe Set()
        cycleGraph.thenRoots mustBe Set()
        complexGraph.thenRoots mustBe Set(c1, a4)

  "GraphStructure.findForward" should:
    "find forward neighbours for given mnIds" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        simpleGraph.findForward(Set(a4)) mustBe Set(Link(a4, a5))

        simpleGraph.findForward(Set(c1)) mustBe Set(
          Then(c1, c2),
          Link(c1, a6),
          Link(c1, a4),
          Then(c1, c2),
          Then(c1, c3)
        )

  "GraphStructure.findBackward" should:
    "find backward neighbours for given mnIds" in newCase[CaseData]: (tn, data) =>
      import data.{c1, c2, c3, simpleGraph}
      async[IO]:
        simpleGraph.findBackward(Set(c2)) mustBe Set(Then(c1, c2))
        simpleGraph.findBackward(Set(c3)) mustBe Set(Then(c1, c3))
  
  "GraphStructure.empty" should:
    "construct empty GraphStructure" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = GraphStructure.empty[IO]
        graph.keys mustBe Set.empty
        graph.srcMap mustBe Map.empty
        graph.trgMap mustBe Map.empty

  "GraphStructure.apply(Set[Edge])" should:
    "construct GraphStructure from edges" in newCase[CaseData]: (tn, data) =>
      import data.*
      val graph = GraphStructure[IO](simpleEnds)
      async[IO]:
        graph.keys mustBe simpleEnds

        graph.srcMap mustBe Map(
          c1 -> Set(Then.End(c2), Then.End(c3), Link.End(a4), Link.End(a6)),
          a4 -> Set(Link.End(a5))
        )

        graph.trgMap mustBe Map(
          c2 -> Set(Then.End(c1)),
          c3 -> Set(Then.End(c1)),
          a4 -> Set(Link.End(c1)),
          a5 -> Set(Link.End(a4)),
          a6 -> Set(Link.End(c1))
        )
