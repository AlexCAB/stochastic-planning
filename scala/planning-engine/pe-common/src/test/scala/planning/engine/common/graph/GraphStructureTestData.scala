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
| created: 2026-03-01 |||||||||||*/

package planning.engine.common.graph

import cats.effect.IO
import cats.data.NonEmptyChain

import planning.engine.common.values.node.MnId
import planning.engine.common.graph.edges.MeKey

import org.scalatest.matchers.must.Matchers.*

trait GraphStructureTestData:
  import MeKey.{End, Link, Then}
  import MnId.{Abs, Con}

  lazy val c1 = Con(1)
  lazy val c2 = Con(2)
  lazy val c3 = Con(3)

  lazy val a4 = Abs(4)
  lazy val a5 = Abs(5)
  lazy val a6 = Abs(6)
  lazy val a7 = Abs(7)
  lazy val a8 = Abs(8)
  lazy val a9 = Abs(9)
  lazy val a10 = Abs(10)

  def graphStructure(edges: MeKey*): GraphStructure[IO] = GraphStructure[IO](edges.toSet)

  lazy val conGraph = graphStructure(Link(c1, a4), Link(a4, a5))
  lazy val nonConGraph = graphStructure(Link(c1, a4), Link(c2, a5))
  lazy val cycleGraph = graphStructure(Then(c1, c2), Then(c2, c1))

  lazy val simpleEnds = Set(
    Link(c1, a4),
    Link(a4, a5),
    Link(c1, a6),
    Then(c1, c2),
    Then(c1, c3)
  )

  lazy val simpleGraph = GraphStructure[IO](simpleEnds)

  lazy val complexGraph = graphStructure(
    Link(c1, a4),
    Link(c2, a4),
    Link(c3, a5),
    Link(a4, a6),
    Link(a5, a6),
    Then(c1, c2),
    Then(c2, c3),
    Then(a4, a5),
    Then(a6, a6)
  )

  lazy val invalidLinkGraph = graphStructure(
    Link(c1, a4),
    Link(a4, a5),
    Link(a5, a6),
    Link(a6, a4)
  )

  lazy val nooseGraph = graphStructure(
    Link(c1, a4),
    Link(c2, a4),
    Then(c1, c2),
    Then(c2, c3),
    Then(c3, c3),
    Then(c3, c2)
  )

  lazy val thenPathExamplesGraph = graphStructure(
    Link(c1, a4),
    Link(c2, a4),
    // Direct path with no loops:
    Then(c1, c2), // Direct([1]-->[2]-->[3]-->(4))
    Then(c2, c3),
    Then(c3, a4),
    // Paths with noose:
    Then(c3, c3), // Noose([1]-->[2]-->[3]-->[3])
    Then(c3, c2), // Noose([1]-->[2]-->[3]-->[2])
    // Isolated loop:
    Then(a5, a5), // Loop([(5)-->(5))
    Then(a6, a7), // Loop([(6)-->(7)-->(8)-->(6))
    Then(a7, a8),
    Then(a8, a6),
    Then(a7, a9), // Loop([(6)-->(7)-->(9)-->(8)-->(6))
    Then(a9, a8)
  )

  def pathWalk(edges: MeKey*): NonEmptyChain[(MnId, End)] = NonEmptyChain
    .fromSeq(edges.map(e => e.src -> e.trgEnd))
    .getOrElse(fail("Edges list must not be empty"))
