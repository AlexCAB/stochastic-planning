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
| created: 2026-10-14 |||||||||||*/

package planning.engine.common.values.plan

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData

class HuangWeightSpec extends UnitSpecWithData:
  private class CaseData extends Case:
    val w1: HuangWeight = new HuangWeight(1L)
    val w3: HuangWeight = new HuangWeight(3L)
    val w10: HuangWeight = new HuangWeight(10L)

  "HuangWeight.init" should:
    "have the maximum Long value and be full" in newCase[CaseData]: (_, _) =>
      async[IO]:
        HuangWeight.init.value mustBe Long.MaxValue
        HuangWeight.init.isFull mustBe true

  "HuangWeight.apply(...)" should:
    "create a weight from a positive value" in newCase[CaseData]: (tn, _) =>
      HuangWeight[IO](5L).logValue(tn).asserting(_.value mustBe 5L)

    "fail for zero value" in newCase[CaseData]: (tn, _) =>
      HuangWeight[IO](0L).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("HuangWeight value must be positive, got: 0"))

    "fail for negative value" in newCase[CaseData]: (tn, _) =>
      HuangWeight[IO](-1L).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("HuangWeight value must be positive, got: -1"))

  "HuangWeight.isFull" should:
    "return false for a weight smaller than the maximum" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        w10.isFull mustBe false
        new HuangWeight(Long.MaxValue - 1L).isFull mustBe false

  "HuangWeight.div(...)" should:
    "split a weight evenly when value is divisible by n" in newCase[CaseData]: (tn, data) =>
      import data.*
      w10.div[IO](5).logValue(tn).asserting(_ mustBe List.fill(5)(new HuangWeight(2L)))

    "spread the remainder over the first parts" in newCase[CaseData]: (tn, data) =>
      import data.*
      w10.div[IO](3).logValue(tn).asserting(_ mustBe List(4L, 3L, 3L).map(new HuangWeight(_)))

    "return the same weight when n is 1" in newCase[CaseData]: (tn, data) =>
      import data.*
      w10.div[IO](1).logValue(tn).asserting(_ mustBe List(w10))

    "split into unit weights when n equals the value" in newCase[CaseData]: (tn, data) =>
      import data.*
      w3.div[IO](3).logValue(tn).asserting(_ mustBe List.fill(3)(w1))

    "preserve the total weight when splitting the init weight" in newCase[CaseData]: (tn, _) =>
      async[IO]:
        val parts = HuangWeight.init.div[IO](7).logValue(tn).await

        parts.size mustBe 7
        parts.map(_.value).sum mustBe Long.MaxValue
        parts.forall(_.value > 0L) mustBe true

    "fail when n is greater than the value" in newCase[CaseData]: (tn, data) =>
      import data.*
      w3.div[IO](4).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Value is too small: 4 > 3"))

  "HuangWeight.add(...)" should:
    "return a weight with the sum of values" in newCase[CaseData]: (tn, data) =>
      import data.*
      w10.add[IO](w3).logValue(tn).asserting(_ mustBe new HuangWeight(13L))

    "restore the init weight after adding back all divided parts" in newCase[CaseData]: (tn, _) =>
      async[IO]:
        val parts = HuangWeight.init.div[IO](5).logValue(tn).await
        val restored = parts.tail.foldM(parts.head)(_.add[IO](_)).logValue(tn).await

        restored mustBe HuangWeight.init
        restored.isFull mustBe true

    "fail on Long overflow" in newCase[CaseData]: (tn, data) =>
      import data.*
      HuangWeight.init.add[IO](w1).logValue(tn)
        .assertThrowsError[ArithmeticException](_.getMessage must include("long overflow"))

    "allow adding to a zero weight" in newCase[CaseData]: (tn, data) =>
      import data.*
      HuangWeight.zero.add[IO](w3).logValue(tn).asserting(_ mustBe w3)

    "fail when the added weight is zero" in newCase[CaseData]: (tn, data) =>
      import data.*
      w10.add[IO](HuangWeight.zero).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("HuangWeight value must be positive, got: 0"))

    "fail when the added weight is negative" in newCase[CaseData]: (tn, data) =>
      import data.*
      w10.add[IO](new HuangWeight(-1L)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("HuangWeight value must be positive, got: -1"))
