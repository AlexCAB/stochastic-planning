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
import planning.engine.common.graph.edges.EdgeKey

import org.scalatest.matchers.must.Matchers.*

trait GraphStructureTestData:
  import EdgeKey.{End, Link, Then}
  import MnId.{Abs, Con}

  lazy val c1 = Con(1)
  lazy val c2 = Con(2)
  lazy val c3 = Con(3)

  lazy val a4 = Abs(4)
  lazy val a5 = Abs(5)
  lazy val a6 = Abs(6)

  def graphStructure(edges: EdgeKey*): GraphStructure[IO] = GraphStructure[IO](edges.toSet)

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

  def pathWalk(edges: EdgeKey*): NonEmptyChain[(MnId, End)] = NonEmptyChain
    .fromSeq(edges.map(e => e.src -> e.trgEnd))
    .getOrElse(fail("Edges list must not be empty"))
