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

package planning.engine.common.values.io

import planning.engine.common.UnitSpecIO

class IoTimeSpec extends UnitSpecIO:
  lazy val testIoTime: IoTime = IoTime(1L)

  "HnId.increase" should:
    "return IoTime with increased value" in: _ =>
      val increased = testIoTime.increase
      increased.value mustBe (testIoTime.value + 1L)

  "HnId.repr" should:
    "return string representation of IoTime" in: _ =>
      testIoTime.repr mustBe s"t=1"

  "HnId.init" should:
    "return IoTime with value 0" in: _ =>
      IoTime.init mustBe IoTime(0L)
