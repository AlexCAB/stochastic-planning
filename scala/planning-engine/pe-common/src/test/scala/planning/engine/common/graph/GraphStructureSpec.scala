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
import planning.engine.common.values.edge.EdgeKey.{End, Link, Then}
import planning.engine.common.values.node.MnId.{Abs, Con}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.edge.EdgeKey

class GraphStructureSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val n1 = Con(1)
    lazy val n2 = Con(2)
    lazy val n3 = Con(3)

    lazy val n4 = Abs(4)
    lazy val n5 = Abs(5)
    lazy val n6 = Abs(6)

    lazy val conGraph = GraphStructure[IO](Set(Link(n1, n2), Link(n2, n3)))
    lazy val nonConGraph = GraphStructure[IO](Set(Link(n1, n2), Link(n3, n4)))
    lazy val cycleGraph = GraphStructure[IO](Set(Link(n1, n2), Link(n2, n1)))

    lazy val sampleEnds = Set(Link(n1, n2), Link(n2, n3), Link(n1, n4), Then(n1, n2), Then(n1, n3))
    lazy val sampleGraph = GraphStructure[IO](sampleEnds)

    lazy val complexGraph = GraphStructure[IO](Set(
      Link(n1, n4),
      Link(n2, n4),
      Link(n3, n5),
      Link(n4, n6),
      Link(n5, n6),
      Then(n1, n2),
      Then(n2, n3),
      Then(n4, n5),
      Then(n6, n6)
    ))

  "GraphStructure.mnIds" should:
    "return all MnIds in the graph" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}
      sampleGraph.mnIds.pure[IO].logValue(tn).asserting(_ mustBe Set(n1, n2, n3, n4))

  "GraphStructure.filterByEndType(...)" should:
    "filter ends by given type" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}
      async[IO]:
        val inMap: Map[MnId, Set[EdgeKey.End]] = Map(
          n1 -> Set(Link.End(n1), Then.End(n1)),
          n2 -> Set(Link.End(n2)),
          n3 -> Set(Then.End(n3))
        )

        sampleGraph.filterByEndType[Link.End](inMap) mustBe Map(n1 -> Set(Link.End(n1)), n2 -> Set(Link.End(n2)))
        sampleGraph.filterByEndType[Then.End](inMap) mustBe Map(n1 -> Set(Then.End(n1)), n3 -> Set(Then.End(n3)))

  "GraphStructure.srcLinkMap" should:
    "return source to target Link ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.srcLinkMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n1 -> Set(Link.End(n2), Link.End(n4)),
        n2 -> Set(Link.End(n3))
      ))

  "GraphStructure.srcThenMap" should:
    "return source to target Then ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}
      sampleGraph.srcThenMap.pure[IO].logValue(tn).asserting(_ mustBe Map(n1 -> Set(Then.End(n2), Then.End(n3))))

  "GraphStructure.trgLinkMap" should:
    "return target to source Link ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.trgLinkMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n2 -> Set(Link.End(n1)),
        n3 -> Set(Link.End(n2)),
        n4 -> Set(Link.End(n1))
      ))

  "GraphStructure.trgThenMap" should:
    "return target to source Then ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}

      sampleGraph.trgThenMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n2 -> Set(Then.End(n1)),
        n3 -> Set(Then.End(n1))
      ))

  "GraphStructure.neighbours" should:
    "return neighbours mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.neighbours.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n1 -> Set(n2, n3, n4),
        n2 -> Set(n1, n3),
        n3 -> Set(n1, n2),
        n4 -> Set(n1)
      ))

  "GraphStructure.findConnected" should:
    "return set of connected MnIds for given hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, conGraph, nonConGraph}
      async[IO]:
        conGraph.findConnected(n1, Set.empty) mustBe Set(n1, n2, n3)
        nonConGraph.findConnected(n1, Set.empty) mustBe Set(n1, n2)

  "GraphStructure.isConnected" should:
    "return whether the graph is connected" in newCase[CaseData]: (tn, data) =>
      import data.{conGraph, nonConGraph}
      async[IO]:
        conGraph.isConnected mustBe true
        nonConGraph.isConnected mustBe false

  "GraphStructure.add" should:
    "add new edges to the graph" in newCase[CaseData]: (tn, data) =>
      import data.{n2, n3, n4, sampleGraph}
      async[IO]:
        val newEdges = Set(Link(n2, n4), Link(n3, n4))
        val newGraph: GraphStructure[IO] = sampleGraph.add(newEdges).await
        newGraph.keys mustBe (sampleGraph.keys ++ newEdges)

  "GraphStructure.findNextEdges(...)" should:
    "return next edges from given hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}
      async[IO]:
        sampleGraph.findNextEdges(Set(n1)) mustBe Set((n1, n2), (n1, n4), (n1, n3))
        sampleGraph.findNextEdges(Set(n2)) mustBe Set((n2, n3))
        sampleGraph.findNextEdges(Set(n3)) mustBe Set()
        sampleGraph.findNextEdges(Set(n1, n2)) mustBe Set((n1, n2), (n1, n4), (n1, n3), (n2, n3))

  "GraphStructure.traceFromNodes(...)" should:
    "trace abstract nodes from connected hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph, cycleGraph}
      async[IO]:
        sampleGraph.traceFromNodes(Set(n1)) mustBe (true, Set(n2, n3, n4))
        cycleGraph.traceFromNodes(Set(n1)) mustBe (false, Set(n1, n2))

  "GraphStructure.linkRoots" should:
    "return link root hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph, conGraph, nonConGraph, cycleGraph, complexGraph}
      async[IO]:
        sampleGraph.linkRoots mustBe Set(n1)
        conGraph.linkRoots mustBe Set(n1)
        nonConGraph.linkRoots mustBe Set(n1, n3)
        cycleGraph.linkRoots mustBe Set()
        complexGraph.linkRoots mustBe Set(n1, n2, n3)

  "GraphStructure.thenRoots" should:
    "return then root hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n4, sampleGraph, conGraph, nonConGraph, cycleGraph, complexGraph}
      async[IO]:
        sampleGraph.thenRoots mustBe Set(n1)
        conGraph.thenRoots mustBe Set()
        nonConGraph.thenRoots mustBe Set()
        cycleGraph.thenRoots mustBe Set()
        complexGraph.thenRoots mustBe Set(n1, n4)

  "GraphStructure.findForward" should:
    "find forward neighbours for given hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}
      async[IO]:
        sampleGraph.findForward(Set(n1)) mustBe Set(Link(n1, n2), Link(n1, n4), Then(n1, n2), Then(n1, n3))
        sampleGraph.findForward(Set(n2)) mustBe Set(Link(n2, n3))

  "GraphStructure.findBackward" should:
    "find backward neighbours for given hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}
      async[IO]:
        sampleGraph.findBackward(Set(n2)) mustBe Set(Link(n1, n2), Then(n1, n2))
        sampleGraph.findBackward(Set(n3)) mustBe Set(Link(n2, n3), Then(n1, n3))

  "GraphStructure.empty" should:
    "construct empty GraphStructure" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val graph = GraphStructure.empty[IO]
        graph.keys mustBe Set.empty
        graph.srcMap mustBe Map.empty
        graph.trgMap mustBe Map.empty

  "GraphStructure.apply(Set[Edge])" should:
    "construct GraphStructure from edges" in newCase[CaseData]: (tn, data) =>
      import data.{sampleEnds, n1, n2, n3, n4}
      val graph = GraphStructure[IO](sampleEnds)
      async[IO]:
        graph.keys mustBe sampleEnds

        graph.srcMap mustBe Map(
          n1 -> Set(Link.End(n2), Then.End(n2), Then.End(n3), Link.End(n4)),
          n2 -> Set(Link.End(n3))
        )
        graph.trgMap mustBe Map(
          n2 -> Set(Link.End(n1), Then.End(n1)),
          n3 -> Set(Then.End(n1), Link.End(n2)),
          n4 -> Set(Link.End(n1))
        )
