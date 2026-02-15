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

package planning.engine.common.values.edge

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.edge.EdgeKey.{Link, Then}
import planning.engine.common.values.node.MnId.{Con, Abs}

class KeySpec extends AnyWordSpec with Matchers:
  lazy val edgeLink = Link(Abs(3), Con(4))
  lazy val edgeThen = Then(Con(5), Abs(6))

  "Key.asEdgeType" should:
    "return EdgeType.LINK for Link edge" in:
      edgeLink.asEdgeType mustBe EdgeType.LINK

    "return EdgeType.THEN for Then edge" in:
      edgeThen.asEdgeType mustBe EdgeType.THEN

  "Key.repr" should:
    "return correct string representation for Link edge" in:
      edgeLink.repr mustBe "(3) -link-> [4]"

    "return correct string representation for Then edge" in:
      edgeThen.repr mustBe "[5] -then-> (6)"

  "Key.Link.srcEnd" should:
    "return source end as End.Link" in:
      edgeLink.srcEnd mustBe Link.End(edgeLink.src)

  "Key.Link.trgEnd" should:
    "return target end as End.Link" in:
      edgeLink.trgEnd mustBe Link.End(edgeLink.trg)

  "Key.Link.asSrcEnds" should:
    "return new Link ends with given source" in:
      edgeLink.trgEnd.asSrcKey(Abs(7)) mustBe Link(Abs(7), edgeLink.trg)
      
  "Key.Link.asTrgKey" should:
    "return new Link ends with given target" in:
      edgeLink.srcEnd.asTrgKey(Con(8)) mustBe Link(edgeLink.src, Con(8))
    
  "Key.Then.srcEnd" should:
    "return source end as End.Then" in:
      edgeThen.srcEnd mustBe Then.End(edgeThen.src)

  "Key.Then.trgEnd" should:
    "return target end as End.Then" in:
      edgeThen.trgEnd mustBe Then.End(edgeThen.trg)

  "Key.Then.asSrcEnds" should:
    "return new Then ends with given source" in:
      edgeThen.trgEnd.asSrcKey(Con(8)) mustBe Then(Con(8), edgeThen.trg)
      
  "Key.Then.asTrgKey" should:
    "return new Then ends with given target" in:
      edgeThen.srcEnd.asTrgKey(Abs(7)) mustBe Then(edgeThen.src, Abs(7))
