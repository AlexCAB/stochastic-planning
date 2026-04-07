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
import planning.engine.common.graph.edges.MeKey.{End, Link, Then}
import planning.engine.common.values.node.MnId

class MapPathhSpec extends UnitSpecIO:
  lazy val n1: Abs = Abs(1L)
  lazy val n2: Con = Con(2L)
  lazy val n3: Con = Con(3L)
  lazy val n4: Abs = Abs(4L)

  lazy val correctWalk: Vector[(MnId, End)] = Vector((n1, Link.End(n2)), (n2, Then.End(n3)), (n3, Link.End(n4)))
  lazy val disruptWalk: Vector[(MnId, End)] = Vector((n1, Link.End(n2)), (n3, Link.End(n4)))

  lazy val pathDirect = new MapPath.Direct(NonEmptyChain.fromSeq(correctWalk).get)
  lazy val pathLoop = new MapPath.Loop(NonEmptyChain.fromSeq(correctWalk).get)
  lazy val pathNoose = new MapPath.Noose(NonEmptyChain.fromSeq(correctWalk).get)

  "MapPath.begin" should:
    "return source MnId of the first edge" in: _ =>
      pathDirect.begin mustBe n1

  "MapPath.end" should:
    "return target MnId of the last edge" in: _ =>
      pathDirect.end mustBe n4

  "MapPath.reprType" should:
    "return 'Direct' for Direct path" in: _ =>
      pathDirect.reprType mustBe "Direct"

    "return 'Loop' for Loop path" in: _ =>
      pathLoop.reprType mustBe "Loop"

    "return 'Noose' for Noose path" in: _ =>
      pathNoose.reprType mustBe "Noose"

  "MapPath.reprChain" should:
    "return a string representation of the path chain" in: _ =>
      pathDirect.reprChain mustBe "(1)==>[2]-->[3]==>(4)"

  "MapPath.makePath" should:
    "create a Path if the walk is continuous" in: _ =>
      MapPath.makePath[IO, MapPath.Direct](correctWalk, MapPath.Direct.apply).asserting(_ mustBe pathDirect)

    "fail if the walk is not continuous" in: _ =>
      MapPath.makePath[IO, MapPath.Direct](disruptWalk, MapPath.Direct.apply)
        .assertThrowsError[AssertionError](_.getMessage must include("Path must be continuous"))

    "fail if the walk is empty" in: _ =>
      MapPath.makePath[IO, MapPath.Direct](Vector.empty, MapPath.Direct.apply)
        .assertThrowsError[AssertionError](_.getMessage must include("Path must contain at least one edge"))

  "MapPath.Direct.apply(...)" should:
    "create a Direct path if the walk is continuous" in: _ =>
      MapPath.Direct[IO](correctWalk).asserting(_ mustBe pathDirect)

  "MapPath.Loop.apply(...)" should:
    "create a Loop path if the walk is continuous" in: _ =>
      MapPath.Loop[IO](correctWalk).asserting(_ mustBe pathLoop)

  "MapPath.Noose.apply(...)" should:
    "create a Noose path if the walk is continuous" in: _ =>
      MapPath.Noose[IO](correctWalk).asserting(_ mustBe pathNoose)
