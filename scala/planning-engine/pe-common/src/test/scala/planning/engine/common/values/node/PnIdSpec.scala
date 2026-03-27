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

class PnIdSpec extends UnitSpecIO:
  lazy val conPnId = PnId.Con(MnId.Con(123L), IoTime(432L))
  lazy val absPnId = PnId.Abs(MnId.Abs(321L), IoTime(234L))
  lazy val pnIdSet = Set(conPnId, absPnId)

  "PnId.repr" should:
    "return correct string representation" in: _ =>
      conPnId.repr mustBe "[123_t432]"
      absPnId.repr mustBe "(321_t234)"

  "PnId.filterCon" should:
    "filter only Con PnIds" in: _ =>
      pnIdSet.filterCon mustBe Set(conPnId)

  "PnId.filterAbs" should:
    "filter only Abs PnIds" in: _ =>
      pnIdSet.filterAbs mustBe Set(absPnId)
