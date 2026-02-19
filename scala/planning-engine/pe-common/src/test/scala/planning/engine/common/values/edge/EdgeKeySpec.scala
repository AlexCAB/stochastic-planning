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

import cats.effect.IO
import cats.syntax.all.*

import planning.engine.common.UnitSpecIO
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.edge.EdgeKey.{Link, Then}
import planning.engine.common.values.node.MnId.{Abs, Con}

class EdgeKeySpec extends UnitSpecIO:
  lazy val n1: Abs = Abs(1L)
  lazy val n2: Con = Con(2L)
  lazy val n3: Con = Con(3L)
  lazy val n4: Abs = Abs(4L)

  lazy val edgeLink = Link(n1, n2)
  lazy val edgeThen = Then(n3, n4)

  "EdgeKey.isLink" should:
    "return true for Link edge" in: _ =>
      edgeLink.isLink.pure[IO].asserting(_ mustBe true)

    "return false for Then edge" in: _ =>
      edgeThen.isLink.pure[IO].asserting(_ mustBe false)

  "EdgeKey.isThen" should:
    "return false for Link edge" in: _ =>
      edgeLink.isThen.pure[IO].asserting(_ mustBe false)

    "return true for Then edge" in: _ =>
      edgeThen.isThen.pure[IO].asserting(_ mustBe true)

  "EdgeKey.asEdgeType" should:
    "return EdgeType.LINK for Link edge" in: _ =>
      edgeLink.asEdgeType.pure[IO].asserting(_ mustBe EdgeType.LINK)

    "return EdgeType.THEN for Then edge" in: _ =>
      edgeThen.asEdgeType.pure[IO].asserting(_ mustBe EdgeType.THEN)

  "EdgeKey.repr" should:
    "return correct string representation for Link edge" in: _ =>
      edgeLink.repr.pure[IO].asserting(_ mustBe "(1) -link-> [2]")

    "return correct string representation for Then edge" in: _ =>
      edgeThen.repr.pure[IO].asserting(_ mustBe "[3] -then-> (4)")

  "EdgeKey.Link.srcEnd" should:
    "return source end as End.Link" in: _ =>
      edgeLink.srcEnd.pure[IO].asserting(_ mustBe Link.End(edgeLink.src))

  "EdgeKey.Link.trgEnd" should:
    "return target end as End.Link" in: _ =>
      edgeLink.trgEnd.pure[IO].asserting(_ mustBe Link.End(edgeLink.trg))

  "EdgeKey.Link.asSrcEnds" should:
    "return new Link ends with given source" in: _ =>
      edgeLink.trgEnd.asSrcKey(Abs(7)).pure[IO].asserting(_ mustBe Link(Abs(7), edgeLink.trg))

  "EdgeKey.Link.asTrgKey" should:
    "return new Link ends with given target" in: _ =>
      edgeLink.srcEnd.asTrgKey(Con(8)).pure[IO].asserting(_ mustBe Link(edgeLink.src, Con(8)))

  "EdgeKey.Then.srcEnd" should:
    "return source end as End.Then" in: _ =>
      edgeThen.srcEnd.pure[IO].asserting(_ mustBe Then.End(edgeThen.src))

  "EdgeKey.Then.trgEnd" should:
    "return target end as End.Then" in: _ =>
      edgeThen.trgEnd.pure[IO].asserting(_ mustBe Then.End(edgeThen.trg))

  "EdgeKey.Then.asSrcEnds" should:
    "return new Then ends with given source" in: _ =>
      edgeThen.trgEnd.asSrcKey(Con(8)).pure[IO].asserting(_ mustBe Then(Con(8), edgeThen.trg))

  "EdgeKey.Then.asTrgKey" should:
    "return new Then ends with given target" in: _ =>
      edgeThen.srcEnd.asTrgKey(Abs(7)).pure[IO].asserting(_ mustBe Then(edgeThen.src, Abs(7)))

  "EdgeKey.apply" should:
    "create Link edge from EdgeType.LINK" in: _ =>
      EdgeKey[IO](EdgeType.LINK, n1.asHnId, n2.asHnId, Set(n2), Set(n1)).asserting(_ mustBe edgeLink)

    "create Then edge from EdgeType.THEN" in: _ =>
      EdgeKey[IO](EdgeType.THEN, n3.asHnId, n4.asHnId, Set(n3), Set(n4)).asserting(_ mustBe edgeThen)
