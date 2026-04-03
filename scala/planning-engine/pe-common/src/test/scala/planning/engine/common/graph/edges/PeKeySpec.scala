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
| created: 2026-03-24 |||||||||||*/

package planning.engine.common.graph.edges

import planning.engine.common.UnitSpecIO
import planning.engine.common.values.node.{PnId, MnId}

class PeKeySpec extends UnitSpecIO:
  import PeKey.{Link, Then}

  lazy val conPnId = PnId.Con(MnId.Con(1L), 0L)
  lazy val absPnId = PnId.Abs(MnId.Abs(2L), 0L)

  lazy val linkKey = Link(conPnId, absPnId)
  lazy val thenKey = Then(absPnId, conPnId)

  "PeKey.repr" should:
    "return correct string representation for edge" in: _ =>
      linkKey.reprArrow mustBe "==>"
      thenKey.reprArrow mustBe "-->"

  "PeKey.toString" should:
    "have correct string representation" in: _ =>
      linkKey.toString mustBe "[1_i=0]==>(2_i=0)"
      thenKey.toString mustBe "(2_i=0)-->[1_i=0]"
