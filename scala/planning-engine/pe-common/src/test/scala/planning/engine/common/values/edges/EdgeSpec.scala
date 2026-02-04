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
| created: 2026-01-26 |||||||||||*/

package planning.engine.common.values.edges

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.edges.Edge.*
import planning.engine.common.values.node.HnId

class EdgeSpec extends AnyWordSpec with Matchers:
  lazy val ends = Ends(HnId(1), HnId(2))
  lazy val edgeLink = Link(HnId(3), HnId(4))
  lazy val edgeThen = Then(HnId(5), HnId(6))

  "Edge.ends" should:
    "return correct ends for Link edge" in:
      edgeLink.ends.src mustBe edgeLink.src
      edgeLink.ends.trg mustBe edgeLink.trg

    "return correct ends for Then edge" in:
      edgeThen.ends.src mustBe edgeThen.src
      edgeThen.ends.trg mustBe edgeThen.trg

  "Edge.eType" should:
    "return EdgeType.LINK for Link edge" in:
      edgeLink.eType mustBe EdgeType.LINK

    "return EdgeType.THEN for Then edge" in:
      edgeThen.eType mustBe EdgeType.THEN

  "Edge.repr" should:
    "return correct string representation for Link edge" in:
      edgeLink.repr mustBe "(3) -link-> (4)"

    "return correct string representation for Then edge" in:
      edgeThen.repr mustBe "(5) -then-> (6)"

  "Edge.Link.srcEnd" should:
    "return source end as End.Link" in:
      edgeLink.srcEnd mustBe End.Link(edgeLink.src)

  "Edge.Link.trgEnd" should:
    "return target end as End.Link" in:
      edgeLink.trgEnd mustBe End.Link(edgeLink.trg)

  "Edge.Then.srcEnd" should:
    "return source end as End.Then" in:
      edgeThen.srcEnd mustBe End.Then(edgeThen.src)

  "Edge.Then.trgEnd" should:
    "return target end as End.Then" in:
      edgeThen.trgEnd mustBe End.Then(edgeThen.trg)

  "Edge.Ends.swap" should:
    "swap source and target ids" in:
      val swapped = ends.swap

      swapped.src mustBe ends.trg
      swapped.trg mustBe ends.src

  "Edge.Ends.toLink" should:
    "create Link edge from ends" in:
      val linkEdge = ends.toLink

      linkEdge.src mustBe ends.src
      linkEdge.trg mustBe ends.trg

  "Edge.Ends.toThen" should:
    "create Then edge from ends" in:
      val thenEdge = ends.toThen

      thenEdge.src mustBe ends.src
      thenEdge.trg mustBe ends.trg
