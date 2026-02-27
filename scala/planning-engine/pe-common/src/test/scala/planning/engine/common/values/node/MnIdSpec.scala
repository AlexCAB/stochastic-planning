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
| created: 2026-02-07 |||||||||||*/

package planning.engine.common.values.node

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecIO

class MnIdSpec extends UnitSpecIO:
  lazy val testConId: MnId.Con = MnId.Con(1L)
  lazy val testAbsId: MnId.Abs = MnId.Abs(2L)

  "MnId.asHnId" should:
    "return correct HnId for Con MnId" in: _ =>
      testConId.asHnId.pure[IO].asserting(_ mustBe HnId(1L))

    "return correct HnId for Abs MnId" in: _ =>
      testAbsId.asHnId.pure[IO].asserting(_ mustBe HnId(2L))

  "MnId.reprValue" should:
    "return string representation of the value for Con MnId" in: _ =>
      testConId.reprValue.pure[IO].asserting(_ mustBe "1")

    "return string representation of the value for Abs MnId" in: _ =>
      testAbsId.reprValue.pure[IO].asserting(_ mustBe "2")

  "MnId.reprNode" should:
    "return string representation of the node for Con MnId" in: _ =>
      testConId.reprNode.pure[IO].asserting(_ mustBe "[1]")

    "return string representation of the node for Abs MnId" in: _ =>
      testAbsId.reprNode.pure[IO].asserting(_ mustBe "(2)")

  "MnId.filterCon" should:
    "filter only Con MnIds from a set" in: _ =>
      Set(testConId, testAbsId).filterCon.pure[IO].asserting(_ mustBe Set(testConId))

  "MnId.filterAbs" should:
    "filter only Abs MnIds from a set" in: _ =>
      Set(testConId, testAbsId).filterAbs.pure[IO].asserting(_ mustBe Set(testAbsId))
