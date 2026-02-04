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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import planning.engine.common.values.edges.Edge.*
import planning.engine.common.values.edges.Edge
import planning.engine.common.values.edges.End
import planning.engine.common.values.node.HnId

class EndsGraphSpec extends AnyWordSpec with Matchers:

  lazy val n1 = HnId(1)
  lazy val n2 = HnId(2)
  lazy val n3 = HnId(3)

  lazy val n4 = HnId(4)
  lazy val n5 = HnId(5)

  lazy val n6 = HnId(6)

  def makeEndsGraph(es: Edge*) = new EndsGraph(es.toSet) {}

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

  "EndsGraph.edgeSet" should:
    "return all edges in the graph" in:
      conGraph.edgeSet mustBe Set(Link(n1, n2), Link(n2, n3))

  "EndsGraph.allEdgesHnId" should:
    "return all hnIds present in edges" in:
      conGraph.allEdgesHnId mustBe Set(n1, n2, n3)

  "EndsGraph.findConnected" should:
    "find all connected hnIds from a starting hnId" in:
      conGraph.findConnected(n1, Set()) mustBe Set(n1, n2, n3)
      conGraph.findConnected(n3, Set()) mustBe Set(n1, n2, n3)
      nonConGraph.findConnected(n1, Set()) mustBe Set(n1, n2)
      nonConGraph.findConnected(n3, Set()) mustBe Set(n3, n4)

  "EndsGraph.srcMap" should:
    "return source to target mapping" in:
      sampleGraph.srcMap mustBe Map(
        n1 -> Set(End.Link(n2), End.Link(n4), End.Then(n2), End.Then(n3)),
        n2 -> Set(End.Link(n3))
      )

  "EndsGraph.trgMap" should:
    "return target to source mapping" in:
      sampleGraph.trgMap mustBe Map(
        n2 -> Set(End.Then(n1), End.Link(n1)),
        n3 -> Set(End.Then(n1), End.Link(n2)),
        n4 -> Set(End.Link(n1))
      )

  "EndsGraph.filterByEndType(...)" should:
    "filter ends by given type" in:
      val inMap = Map(n1 -> Set(End.Link(n1), End.Then(n1)), n2 -> Set(End.Link(n2)), n3 -> Set(End.Then(n3)))
      sampleGraph.filterByEndType[End.Link](inMap) mustBe Map(n1 -> Set(End.Link(n1)), n2 -> Set(End.Link(n2)))
      sampleGraph.filterByEndType[End.Then](inMap) mustBe Map(n1 -> Set(End.Then(n1)), n3 -> Set(End.Then(n3)))

  "EndsGraph.srcLinkMap" should:
    "return source to target Link ends mapping" in:
      sampleGraph.srcLinkMap mustBe Map(n1 -> Set(End.Link(n2), End.Link(n4)), n2 -> Set(End.Link(n3)))

  "EndsGraph.srcThenMap" should:
    "return source to target Then ends mapping" in:
      sampleGraph.srcThenMap mustBe Map(n1 -> Set(End.Then(n2), End.Then(n3)))

  "EndsGraph.trgLinkMap" should:
    "return target to source Link ends mapping" in:
      sampleGraph.trgLinkMap mustBe Map(n2 -> Set(End.Link(n1)), n3 -> Set(End.Link(n2)), n4 -> Set(End.Link(n1)))

  "EndsGraph.trgThenMap" should:
    "return target to source Then ends mapping" in:
      sampleGraph.trgThenMap mustBe Map(n2 -> Set(End.Then(n1)), n3 -> Set(End.Then(n1)))

  "EndsGraph.neighbours" should:
    "return neighbours mapping" in:
      sampleGraph.neighbours mustBe Map(
        n1 -> Set(n2, n3, n4),
        n2 -> Set(n1, n3),
        n3 -> Set(n1, n2),
        n4 -> Set(n1)
      )

  "EndsGraph.isConnected" should:
    "return whether the graph is connected" in:
      conGraph.isConnected mustBe true
      nonConGraph.isConnected mustBe false

  "EndsGraph.findNextEdges(...)" should:
    "return next edges from given hnIds" in:
      sampleGraph.findNextEdges(Set(n1)) mustBe Set((n1, n2), (n1, n4), (n1, n3))
      sampleGraph.findNextEdges(Set(n2)) mustBe Set((n2, n3))
      sampleGraph.findNextEdges(Set(n3)) mustBe Set()
      sampleGraph.findNextEdges(Set(n1, n2)) mustBe Set((n1, n2), (n1, n4), (n1, n3), (n2, n3))

  "EndsGraph.traceFromNodes(...)" should:
    "trace abstract nodes from connected hnIds" in:
      sampleGraph.traceFromNodes(Set(n1)) mustBe (true, Set(n2, n3, n4))
      cycleGraph.traceFromNodes(Set(n1)) mustBe (false, Set(n1, n2))

  "EndsGraph.linkRoots" should:
    "return link root hnIds" in:
      sampleGraph.linkRoots mustBe Set(n1)
      conGraph.linkRoots mustBe Set(n1)
      nonConGraph.linkRoots mustBe Set(n1, n3)
      cycleGraph.linkRoots mustBe Set()
      complexGraph.linkRoots mustBe Set(n1, n2, n3)

  "EndsGraph.thenRoots" should:
    "return then root hnIds" in:
      sampleGraph.thenRoots mustBe Set(n1)
      conGraph.thenRoots mustBe Set()
      nonConGraph.thenRoots mustBe Set()
      cycleGraph.thenRoots mustBe Set()
      complexGraph.thenRoots mustBe Set(n1, n4)
