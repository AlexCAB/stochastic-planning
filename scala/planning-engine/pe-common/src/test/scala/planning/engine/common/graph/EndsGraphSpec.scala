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
import planning.engine.common.values.edges.EndIds
import planning.engine.common.values.node.ConId

class EndsGraphSpec extends AnyWordSpec with Matchers:

  lazy val n1 = ConId(1)
  lazy val n2 = ConId(2)
  lazy val n3 = ConId(3)
  lazy val n4 = ConId(4)

  lazy val sampleEnds = Set(EndIds(n1, n2), EndIds(n2, n3), EndIds(n1, n4))
  lazy val endsGraph = new EndsGraph(sampleEnds) {}

  "EndsGraph.findConnected" should:
    "find all connected hnIds from a starting hnId" in:
      val nonConGraph = new EndsGraph(Set(EndIds(n1, n2), EndIds(n3, n4))) {}

      endsGraph.findConnected(n1, Set()) mustBe Set(n1, n2, n3, n4)
      endsGraph.findConnected(n4, Set()) mustBe Set(n1, n2, n3, n4)
      nonConGraph.findConnected(n1, Set()) mustBe Set(n1, n2)
      nonConGraph.findConnected(n3, Set()) mustBe Set(n3, n4)

  "EndsGraph.ends" should:
    "return ends" in:
      endsGraph.ends mustBe sampleEnds

  "EndsGraph.srcMap" should:
    "return source to target mapping" in:
      endsGraph.srcMap mustBe Map(n1 -> Set(n2, n4), n2 -> Set(n3))

  "EndsGraph.trgMap" should:
    "return target to source mapping" in:
      endsGraph.trgMap mustBe Map(n2 -> Set(n1), n3 -> Set(n2), n4 -> Set(n1))

  "EndsGraph.neighbours" should:
    "return neighbours mapping" in:
      endsGraph.neighbours mustBe Map(n1 -> Set(n2, n4), n2 -> Set(n1, n3), n3 -> Set(n2), n4 -> Set(n1))

  "EndsGraph.isConnected" should:
    "return whether the graph is connected" in:
      val nonConGraph = new EndsGraph(Set(EndIds(n1, n2), EndIds(n3, n4))) {}

      endsGraph.isConnected mustBe true
      nonConGraph.isConnected mustBe false

  "EndsGraph.findNextEdges(...)" should:
    "return next edges from given hnIds" in:
      endsGraph.findNextEdges(Set(n1)) mustBe Set((n1, n2), (n1, n4))
      endsGraph.findNextEdges(Set(n2)) mustBe Set((n2, n3))
      endsGraph.findNextEdges(Set(n3)) mustBe Set()
      endsGraph.findNextEdges(Set(n1, n2)) mustBe Set((n1, n2), (n1, n4), (n2, n3))

  "EndsGraph.traceFromNodes(...)" should:
    "trace abstract nodes from connected hnIds" in:
      val cycleGraph = new EndsGraph(Set(EndIds(n1, n2), EndIds(n2, n1))) {}

      endsGraph.traceFromNodes(Set(n1)) mustBe (true, Set(n2, n3, n4))
      cycleGraph.traceFromNodes(Set(n1)) mustBe (false, Set(n1, n2))
