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

  "IoTime.increase" should:
    "return IoTime with increased value" in: _ =>
      val increased = testIoTime.increase
      increased.value mustBe (testIoTime.value + 1L)

  "IoTime.repr" should:
    "return string representation of IoTime" in: _ =>
      testIoTime.repr mustBe s"t=1"

  "IoTime.init" should:
    "return IoTime with value 0" in: _ =>
      IoTime.init mustBe IoTime(0L)

  "IoTime.some" should:
    "return Some(IoTime)" in: _ =>
      IoTime.some(5L) mustBe Some(IoTime(5L))

  "Option[IoTime].repr" should:
    "return string representation of Some(IoTime)" in: _ =>
      Some(IoTime(3L)).repr mustBe "t=3"

  "Option[IoTime].getValue" should:
    "return the value of IoTime if Some, otherwise 0" in: _ =>
      Some(IoTime(4L)).getValue mustBe 4L
      Option.empty[IoTime].getValue mustBe Long.MaxValue
