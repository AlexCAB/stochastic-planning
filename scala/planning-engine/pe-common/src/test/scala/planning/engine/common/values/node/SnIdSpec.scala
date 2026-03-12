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
  lazy val testSnId: SnId = SnId(MnId.Con(123L), IoTime(432L))

  "HnId.repr" should:
    "return correct string representation" in: _ =>
      val repr = testSnId.repr
      repr mustBe "[123]_t=432"
