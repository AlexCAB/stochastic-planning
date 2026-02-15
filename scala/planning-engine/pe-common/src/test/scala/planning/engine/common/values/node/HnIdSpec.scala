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
import planning.engine.common.values.node.MnId.{Abs, Con}

class HnIdSpec extends UnitSpecIO:
  lazy val testConId: MnId.Con = MnId.Con(1L)
  lazy val testAbsId: MnId.Abs = MnId.Abs(2L)
  lazy val testHnId1: HnId = testConId.asHnId
  lazy val testHnId2: HnId = testAbsId.asHnId

  "HnId.increase" should:
    "return a new HnId with value increased by 1" in: _ =>
      testHnId1.increase.pure[IO].asserting(_.value mustBe (testHnId1.value + 1))

  "HnId.asCon" should:
    "return a MnId.Con with the same value" in: _ =>
      testHnId1.asCon.pure[IO].asserting(_ mustBe testConId)

  "HnId.asAbs" should:
    "return a MnId.Abs with the same value" in: _ =>
      testHnId2.asAbs.pure[IO].asserting(_ mustBe testAbsId)

  "HnId.toMnId" should:
    "return Con MnId for HnId in conIds set" in: _ =>
      testHnId1.toMnId[IO](Set(testConId), Set.empty).asserting(_ mustBe testConId)

    "return Abs MnId for HnId in absIds set" in: _ =>
      testHnId2.toMnId[IO](Set.empty, Set(testAbsId)).asserting(_ mustBe testAbsId)

    "raise error if HnId is in both conIds and absIds sets" in: _ =>
      testHnId1.toMnId[IO](Set(testConId), Set(testConId.asHnId.asAbs)).assertThrows[AssertionError]

    "raise error if HnId is in neither conIds nor absIds sets" in: _ =>
      testHnId1.toMnId[IO](Set.empty, Set.empty).assertThrows[AssertionError]

  "HnId.init" should:
    "return an HnId with value 1" in: _ =>
      HnId.init.pure[IO].asserting(_ mustBe HnId(1L))
