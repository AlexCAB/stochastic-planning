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
| created: 2026-03-09 |||||||||||*/

package planning.engine.common.graph.edges

import planning.engine.common.UnitSpecIO
import planning.engine.common.graph.edges.EdgeKey.{Link, Then}
import planning.engine.common.values.node.MnId.{Abs, Con}

class EdgeKeySetSpec extends UnitSpecIO:
  lazy val n1: Abs = Abs(1L)
  lazy val n2: Con = Con(2L)
  lazy val n3: Con = Con(3L)
  lazy val n4: Abs = Abs(4L)

  lazy val linkSet: EdgeKeySet[Link] = EdgeKeySet(Link(n1, n2), Link(n3, n4))
  lazy val thenSet: EdgeKeySet[Then] = EdgeKeySet(Then(n1, n2), Then(n3, n4))
  lazy val keySet: EdgeKeySet[EdgeKey] = EdgeKeySet(Link(n1, n2), Then(n3, n4))

  "EdgeKeySet.srcIds" should:
    "return set of source MnIds" in: _ =>
      linkSet.srcIds mustBe Set(n1, n3)
      thenSet.srcIds mustBe Set(n1, n3)
      keySet.srcIds mustBe Set(n1, n3)

  "EdgeKeySet.trgIds" should:
    "return set of target MnIds" in: _ =>
      linkSet.trgIds mustBe Set(n2, n4)
      thenSet.trgIds mustBe Set(n2, n4)
      keySet.trgIds mustBe Set(n2, n4)

  "EdgeKeySet.mnIds" should:
    "return set of all MnIds" in: _ =>
      linkSet.mnIds mustBe Set(n1, n2, n3, n4)
      thenSet.mnIds mustBe Set(n1, n2, n3, n4)
      keySet.mnIds mustBe Set(n1, n2, n3, n4)

  "EdgeKeySet.reprByTrg" should:
    "return list of edge representations sorted by target MnId" in: _ =>
      linkSet.reprByTrg mustBe List("(1)=link=>[2]", "[3]=link=>(4)")
      thenSet.reprByTrg mustBe List("(1)-then->[2]", "[3]-then->(4)")
      keySet.reprByTrg mustBe List("(1)=link=>[2]", "[3]-then->(4)")

  "EdgeKeySet.contains" should:
    "return true for existing edge" in: _ =>
      linkSet.contains(n1, n2) mustBe true
      thenSet.contains(n1, n2) mustBe true
      keySet.contains(n1, n2) mustBe true

    "return false for non-existing edge" in: _ =>
      linkSet.contains(n1, n3) mustBe false
      thenSet.contains(n1, n3) mustBe false
      keySet.contains(n1, n3) mustBe false

  "EdgeKeySet.apply" should:
    "create EdgeKeySet from varargs" in: _ =>
      val set = EdgeKeySet(Link(n1, n2), Link(n3, n4))
      set mustBe linkSet
