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
import planning.engine.common.values.edges.Edge.{End, Link, Then}
import planning.engine.common.values.node.MnId.{Abs, Con}
import planning.engine.common.values.node.MnId
import planning.engine.common.values.edges.Edge

class GraphSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val n1 = Con(1)
    lazy val n2 = Con(2)
    lazy val n3 = Con(3)

    lazy val n4 = Abs(4)
    lazy val n5 = Abs(5)

    lazy val n6 = Abs(6)

    def makeEndsGraph(es: Edge*): GraphStructure[IO] = new GraphStructure[IO]:
      private val ns = es.flatMap(e => Set(e.src, e.trg)).toSet
      override lazy val conNodeSet: Set[MnId.Con] = ns.collect { case c: MnId.Con => c }
      override lazy val absNodeSet: Set[MnId.Abs] = ns.collect { case a: MnId.Abs => a }
      override lazy val edgeSet: Set[Edge] = es.toSet

    lazy val sampleGraph = makeEndsGraph(Link(n1, n2), Link(n2, n3), Link(n1, n4), Then(n1, n2), Then(n1, n3))
    lazy val conGraph = makeEndsGraph(Link(n1, n2), Link(n2, n3))
    lazy val nonConGraph = makeEndsGraph(Link(n1, n2), Link(n3, n4))
    lazy val cycleGraph = makeEndsGraph(Link(n1, n2), Link(n2, n1))

    lazy val complexGraph = makeEndsGraph(
      Link(n1, n4),
      Link(n2, n4),
      Link(n3, n5),
      Link(n4, n6),
      Link(n5, n6),
      Then(n1, n2),
      Then(n2, n3),
      Then(n4, n5),
      Then(n6, n6)
    )

  "EndsGraph.allEdgesHnId" should:
    "return all hnIds present in edges" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, conGraph}
      conGraph.allMnId.pure[IO].logValue(tn).asserting(_ mustBe Set(n1, n2, n3))

  "EndsGraph.findConnected" should:
    "find all connected hnIds from a starting hnId" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, conGraph, nonConGraph}
      async[IO]:
        conGraph.findConnected(n1, Set()) mustBe Set(n1, n2, n3)
        conGraph.findConnected(n3, Set()) mustBe Set(n1, n2, n3)
        nonConGraph.findConnected(n1, Set()) mustBe Set(n1, n2)
        nonConGraph.findConnected(n3, Set()) mustBe Set(n3, n4)

  "EndsGraph.srcMap" should:
    "return source to target mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.srcMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n1 -> Set(Link.End(n2), Link.End(n4), Then.End(n2), Then.End(n3)),
        n2 -> Set(Link.End(n3))
      ))

  "EndsGraph.trgMap" should:
    "return target to source mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.trgMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n2 -> Set(Then.End(n1), Link.End(n1)),
        n3 -> Set(Then.End(n1), Link.End(n2)),
        n4 -> Set(Link.End(n1))
      ))

  "EndsGraph.filterByEndType(...)" should:
    "filter ends by given type" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}
      async[IO]:
        val inMap: Map[MnId, Set[Edge.End]] = Map(
          n1 -> Set(Link.End(n1), Then.End(n1)),
          n2 -> Set(Link.End(n2)),
          n3 -> Set(Then.End(n3))
        )

        sampleGraph.filterByEndType[Link.End](inMap) mustBe Map(n1 -> Set(Link.End(n1)), n2 -> Set(Link.End(n2)))
        sampleGraph.filterByEndType[Then.End](inMap) mustBe Map(n1 -> Set(Then.End(n1)), n3 -> Set(Then.End(n3)))

  "EndsGraph.srcLinkMap" should:
    "return source to target Link ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.srcLinkMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n1 -> Set(Link.End(n2), Link.End(n4)),
        n2 -> Set(Link.End(n3))
      ))

  "EndsGraph.srcThenMap" should:
    "return source to target Then ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}
      sampleGraph.srcThenMap.pure[IO].logValue(tn).asserting(_ mustBe Map(n1 -> Set(Then.End(n2), Then.End(n3))))

  "EndsGraph.trgLinkMap" should:
    "return target to source Link ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.trgLinkMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n2 -> Set(Link.End(n1)),
        n3 -> Set(Link.End(n2)),
        n4 -> Set(Link.End(n1))
      ))

  "EndsGraph.trgThenMap" should:
    "return target to source Then ends mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph}

      sampleGraph.trgThenMap.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n2 -> Set(Then.End(n1)),
        n3 -> Set(Then.End(n1))
      ))

  "EndsGraph.neighbours" should:
    "return neighbours mapping" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}

      sampleGraph.neighbours.pure[IO].logValue(tn).asserting(_ mustBe Map(
        n1 -> Set(n2, n3, n4),
        n2 -> Set(n1, n3),
        n3 -> Set(n1, n2),
        n4 -> Set(n1)
      ))

  "EndsGraph.isConnected" should:
    "return whether the graph is connected" in newCase[CaseData]: (tn, data) =>
      import data.{conGraph, nonConGraph}
      async[IO]:
        conGraph.isConnected mustBe true
        nonConGraph.isConnected mustBe false

  "EndsGraph.findNextEdges(...)" should:
    "return next edges from given hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph}
      async[IO]:
        sampleGraph.findNextEdges(Set(n1)) mustBe Set((n1, n2), (n1, n4), (n1, n3))
        sampleGraph.findNextEdges(Set(n2)) mustBe Set((n2, n3))
        sampleGraph.findNextEdges(Set(n3)) mustBe Set()
        sampleGraph.findNextEdges(Set(n1, n2)) mustBe Set((n1, n2), (n1, n4), (n1, n3), (n2, n3))

  "EndsGraph.traceFromNodes(...)" should:
    "trace abstract nodes from connected hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, n4, sampleGraph, cycleGraph}
      async[IO]:
        sampleGraph.traceFromNodes(Set(n1)) mustBe (true, Set(n2, n3, n4))
        cycleGraph.traceFromNodes(Set(n1)) mustBe (false, Set(n1, n2))

  "EndsGraph.linkRoots" should:
    "return link root hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n2, n3, sampleGraph, conGraph, nonConGraph, cycleGraph, complexGraph}
      async[IO]:
        sampleGraph.linkRoots mustBe Set(n1)
        conGraph.linkRoots mustBe Set(n1)
        nonConGraph.linkRoots mustBe Set(n1, n3)
        cycleGraph.linkRoots mustBe Set()
        complexGraph.linkRoots mustBe Set(n1, n2, n3)

  "EndsGraph.thenRoots" should:
    "return then root hnIds" in newCase[CaseData]: (tn, data) =>
      import data.{n1, n4, sampleGraph, conGraph, nonConGraph, cycleGraph, complexGraph}
      async[IO]:
        sampleGraph.thenRoots mustBe Set(n1)
        conGraph.thenRoots mustBe Set()
        nonConGraph.thenRoots mustBe Set()
        cycleGraph.thenRoots mustBe Set()
        complexGraph.thenRoots mustBe Set(n1, n4)
