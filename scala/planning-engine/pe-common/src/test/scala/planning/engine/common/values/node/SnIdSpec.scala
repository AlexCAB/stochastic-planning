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
| created: 2026-03-12 |||||||||||*/

package planning.engine.common.values.node

import planning.engine.common.UnitSpecIO
import planning.engine.common.values.io.IoTime

class SnIdSpec extends UnitSpecIO:
  lazy val testConPnId = PnId.Con(MnId.Con(123L), IoTime(432L))
  lazy val testAbsPnId = PnId.Abs(MnId.Abs(321L), IoTime(234L))

  "HnId.repr" should:
    "return correct string representation" in: _ =>
      testConPnId.repr mustBe "[123]_t=432"
      testAbsPnId.repr mustBe "(321)_t=234"
