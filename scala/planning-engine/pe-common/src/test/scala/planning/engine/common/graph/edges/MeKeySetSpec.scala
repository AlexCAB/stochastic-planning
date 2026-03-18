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
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.MnId.{Abs, Con}

class MeKeySetSpec extends UnitSpecIO:
  lazy val n1: Abs = Abs(1L)
  lazy val n2: Con = Con(2L)
  lazy val n3: Con = Con(3L)
  lazy val n4: Abs = Abs(4L)

  lazy val linkSet: MeKeySet[Link] = MeKeySet(Link(n1, n2), Link(n3, n4))
  lazy val thenSet: MeKeySet[Then] = MeKeySet(Then(n1, n2), Then(n3, n4))
  lazy val keySet: MeKeySet[MeKey] = MeKeySet(Link(n1, n2), Then(n3, n4))

  "MeKeySet.srcIds" should:
    "return set of source MnIds" in: _ =>
      linkSet.srcIds mustBe Set(n1, n3)
      thenSet.srcIds mustBe Set(n1, n3)
      keySet.srcIds mustBe Set(n1, n3)

  "MeKeySet.trgIds" should:
    "return set of target MnIds" in: _ =>
      linkSet.trgIds mustBe Set(n2, n4)
      thenSet.trgIds mustBe Set(n2, n4)
      keySet.trgIds mustBe Set(n2, n4)

  "MeKeySet.mnIds" should:
    "return set of all MnIds" in: _ =>
      linkSet.mnIds mustBe Set(n1, n2, n3, n4)
      thenSet.mnIds mustBe Set(n1, n2, n3, n4)
      keySet.mnIds mustBe Set(n1, n2, n3, n4)

  "MeKeySet.reprByTrg" should:
    "return list of edge representations sorted by target MnId" in: _ =>
      linkSet.reprByTrg mustBe List("(1)=link=>[2]", "[3]=link=>(4)")
      thenSet.reprByTrg mustBe List("(1)-then->[2]", "[3]-then->(4)")
      keySet.reprByTrg mustBe List("(1)=link=>[2]", "[3]-then->(4)")

  "MeKeySet.contains" should:
    "return true for existing edge" in: _ =>
      linkSet.contains(n1, n2) mustBe true
      thenSet.contains(n1, n2) mustBe true
      keySet.contains(n1, n2) mustBe true

    "return false for non-existing edge" in: _ =>
      linkSet.contains(n1, n3) mustBe false
      thenSet.contains(n1, n3) mustBe false
      keySet.contains(n1, n3) mustBe false

  "MeKeySet.apply" should:
    "create MeKeySet from varargs" in: _ =>
      val set = MeKeySet(Link(n1, n2), Link(n3, n4))
      set mustBe linkSet
