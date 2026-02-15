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
| created: 2026-02-12 |||||||||||*/

package planning.engine.common.values.edge

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.node.{MnId, HnIndex}

class IndexMapSpec extends UnitSpecIO:
  lazy val n1 = MnId.Con(1)
  lazy val n2 = MnId.Abs(1)
  lazy val n3 = MnId.Con(2)

  lazy val i1 = HnIndex(1)
  lazy val i2 = HnIndex(2)
  lazy val i3 = HnIndex(3)

  lazy val indexMap: IndexMap = IndexMap(Map(n1 -> i1, n2 -> i2, n3 -> i3))

  "IndexMap.get(...)" should:
    "return correct HnIndex for existing NId" in: _ =>
      indexMap.get[IO](n1, n2).asserting(_ mustBe Indexies(i1, i2))

    "return None for non-existing NId" in: _ =>
      indexMap.get[IO](n1, MnId.Con(3)).assertThrows[AssertionError]
