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
| created: 2026-02-28 |||||||||||*/

package planning.engine.common.graph.paths

import cats.effect.IO
import cats.data.NonEmptyChain
import planning.engine.common.values.node.MnId.{Abs, Con}
import planning.engine.common.UnitSpecIO
import planning.engine.common.graph.edges.EdgeKey.{End, Link, Then}
import planning.engine.common.values.node.MnId

class PathSpec extends UnitSpecIO:
  lazy val n1: Abs = Abs(1L)
  lazy val n2: Con = Con(2L)
  lazy val n3: Con = Con(3L)
  lazy val n4: Abs = Abs(4L)

  lazy val correctWalk: Vector[(MnId, End)] = Vector((n1, Link.End(n2)), (n2, Then.End(n3)), (n3, Link.End(n4)))
  lazy val disruptWalk: Vector[(MnId, End)] = Vector((n1, Link.End(n2)), (n3, Link.End(n4)))

  lazy val pathDirect = new Path.Direct(NonEmptyChain.fromSeq(correctWalk).get)
  lazy val pathLoop = new Path.Loop(NonEmptyChain.fromSeq(correctWalk).get)
  lazy val pathNoose = new Path.Noose(NonEmptyChain.fromSeq(correctWalk).get)

  "Path.begin" should:
    "return source MnId of the first edge" in: _ =>
      pathDirect.begin mustBe n1

  "Path.end" should:
    "return target MnId of the last edge" in: _ =>
      pathDirect.end mustBe n4

  "Path.makePath" should:
    "create a Path if the walk is continuous" in: _ =>
      Path.makePath[IO, Path.Direct](correctWalk, Path.Direct.apply).asserting(_ mustBe pathDirect)

    "fail if the walk is not continuous" in: _ =>
      Path.makePath[IO, Path.Direct](disruptWalk, Path.Direct.apply)
        .assertThrowsError[AssertionError](_.getMessage must include("Path must be continuous"))

    "fail if the walk is empty" in: _ =>
      Path.makePath[IO, Path.Direct](Vector.empty, Path.Direct.apply)
        .assertThrowsError[AssertionError](_.getMessage must include("Path must contain at least one edge"))

  "Path.Direct.apply(...)" should:
    "create a Direct path if the walk is continuous" in: _ =>
      Path.Direct[IO](correctWalk).asserting(_ mustBe pathDirect)

  "Path.Loop.apply(...)" should:
    "create a Loop path if the walk is continuous" in: _ =>
      Path.Loop[IO](correctWalk).asserting(_ mustBe pathLoop)

  "Path.Noose.apply(...)" should:
    "create a Noose path if the walk is continuous" in: _ =>
      Path.Noose[IO](correctWalk).asserting(_ mustBe pathNoose)
