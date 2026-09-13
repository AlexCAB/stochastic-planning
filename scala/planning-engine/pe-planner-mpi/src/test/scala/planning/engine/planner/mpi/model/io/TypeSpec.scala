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
| created: 2026-09-04 |||||||||||*/

package planning.engine.planner.mpi.model.io

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.IoIndex

class TypeSpec extends UnitSpecWithData:
  private class CaseData extends Case:
    val numType: Type.N = Type.N(-10L, 10L)
    val realType: Type.R = Type.R(-1.0, 1.0)
    val boolType: Type.Bool = Type.Bool(Set(true, false))
    val trueOnlyType = Type.Bool(Set(true))
    val optType: Type.Opt = Type.Opt(List("red", "green", "blue"))

  "Type.N" should:
    "isDefinedAt(...)" should:
      "return true for the min bound, the max bound, and a value in between" in newCase[CaseData]: (_, d) =>
        import d.*
        async[IO]:
          numType.isDefinedAt(IoIndex(-10)) mustBe true
          numType.isDefinedAt(IoIndex(10)) mustBe true
          numType.isDefinedAt(IoIndex(0)) mustBe true

      "return false for a value outside the range" in newCase[CaseData]: (_, d) =>
        import d.*
        async[IO]:
          numType.isDefinedAt(IoIndex(-11)) mustBe false
          numType.isDefinedAt(IoIndex(11)) mustBe false

    "valueForIndex(...)" should:
      "return the index value when it is within range" in newCase[CaseData]: (_, d) =>
        d.numType.valueForIndex[IO](IoIndex(5)).asserting(_ mustBe 5L)

      "raise an error when the index is out of range" in newCase[CaseData]: (tn, d) =>
        d.numType.valueForIndex[IO](IoIndex(11)).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"not in range [-10, 10]"))

    "indexForValue(...)" should:
      "return the IoIndex for a value within range" in newCase[CaseData]: (_, d) =>
        d.numType.indexForValue[IO](5L).asserting(_ mustBe IoIndex(5))

      "raise an error when the value is out of range" in newCase[CaseData]: (tn, d) =>
        d.numType.indexForValue[IO](11L).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include("Value 11 not in range [-10, 10]"))

  "Type.R" should:
    "isDefinedAt(...)" should:
      "return true for the min bound, the max bound, and a value in between" in newCase[CaseData]: (_, d) =>
        import d.*
        async[IO]:
          realType.isDefinedAt(IoIndex(-10000)) mustBe true
          realType.isDefinedAt(IoIndex(10000)) mustBe true
          realType.isDefinedAt(IoIndex(5000)) mustBe true

      "return false for a value outside the range" in newCase[CaseData]: (_, d) =>
        import d.*
        async[IO]:
          realType.isDefinedAt(IoIndex(-10001)) mustBe false
          realType.isDefinedAt(IoIndex(10001)) mustBe false

    "valueForIndex(...)" should:
      "return the scaled value when the index is within range" in newCase[CaseData]: (_, d) =>
        d.realType.valueForIndex[IO](IoIndex(5000)).asserting(_ mustBe 0.5)

      "raise an error when the index is out of range" in newCase[CaseData]: (tn, d) =>
        d.realType.valueForIndex[IO](IoIndex(10001)).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"Value 1.0001 not in range [-1.0, 1.0]"))

    "indexForValue(...)" should:
      "return the scaled IoIndex for a value within range" in newCase[CaseData]: (_, d) =>
        d.realType.indexForValue[IO](0.5).asserting(_ mustBe IoIndex(5000))

      "raise an error when the value is out of range" in newCase[CaseData]: (tn, d) =>
        d.realType.indexForValue[IO](1.0001).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include("Value 1.0001 not in range [-1.0, 1.0]"))

  "Type.Bool" should:
    "isDefinedAt(...)" should:
      "return true for index 0 when false is acceptable" in newCase[CaseData]: (_, d) =>
        d.boolType.isDefinedAt(IoIndex(0)).pure[IO].asserting(_ mustBe true)

      "return true for index 1 when true is acceptable" in newCase[CaseData]: (_, d) =>
        d.boolType.isDefinedAt(IoIndex(1)).pure[IO].asserting(_ mustBe true)

      "return false for index 0 when false is not acceptable" in newCase[CaseData]: (_, d) =>
        d.trueOnlyType.isDefinedAt(IoIndex(0)).pure[IO].asserting(_ mustBe false)

      "return false for an index other than 0 or 1" in newCase[CaseData]: (_, d) =>
        d.boolType.isDefinedAt(IoIndex(2)).pure[IO].asserting(_ mustBe false)

    "valueForIndex(...)" should:
      "return false for index 0" in newCase[CaseData]: (_, d) =>
        d.boolType.valueForIndex[IO](IoIndex(0)).asserting(_ mustBe false)

      "return true for index 1" in newCase[CaseData]: (_, d) =>
        d.boolType.valueForIndex[IO](IoIndex(1)).asserting(_ mustBe true)

      "raise an error for index 0 when false is not acceptable" in newCase[CaseData]: (tn, d) =>
        d.trueOnlyType.valueForIndex[IO](IoIndex(0)).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"or not in acceptable values"))

      "raise an error for an index other than 0 or 1" in newCase[CaseData]: (tn, d) =>
        d.boolType.valueForIndex[IO](IoIndex(2)).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"Invalid index"))

    "indexForValue(...)" should:
      "return IoIndex(0) for false when acceptable" in newCase[CaseData]: (_, d) =>
        d.boolType.indexForValue[IO](false).asserting(_ mustBe IoIndex(0))

      "return IoIndex(1) for true when acceptable" in newCase[CaseData]: (_, d) =>
        d.boolType.indexForValue[IO](true).asserting(_ mustBe IoIndex(1))

      "raise an error when the value is not acceptable" in newCase[CaseData]: (tn, d) =>
        d.trueOnlyType.indexForValue[IO](false).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"Value 'false' not in acceptable values"))

  "Type.Opt" should:
    "isDefinedAt(...)" should:
      "return true for an index within the options list" in newCase[CaseData]: (_, d) =>
        import d.*
        async[IO]:
          optType.isDefinedAt(IoIndex(0)) mustBe true
          optType.isDefinedAt(IoIndex(2)) mustBe true

      "return false for an index outside the options list" in newCase[CaseData]: (_, d) =>
        import d.*
        async[IO]:
          optType.isDefinedAt(IoIndex(3)) mustBe false
          optType.isDefinedAt(IoIndex(-1)) mustBe false

    "valueForIndex(...)" should:
      "return the option at the given index" in newCase[CaseData]: (_, d) =>
        d.optType.valueForIndex[IO](IoIndex(1)).asserting(_ mustBe "green")

      "raise an error when the index is out of bounds" in newCase[CaseData]: (tn, d) =>
        d.optType.valueForIndex[IO](IoIndex(3)).logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"out of bounds for options list of size 3"))

    "indexForValue(...)" should:
      "return the IoIndex for an option in the list" in newCase[CaseData]: (_, d) =>
        d.optType.indexForValue[IO]("blue").asserting(_ mustBe IoIndex(2))

      "raise an error when the value is not in the options list" in newCase[CaseData]: (tn, d) =>
        d.optType.indexForValue[IO]("purple").logValue(tn)
          .assertThrowsError[AssertionError](_.getMessage must include(s"Value purple not in options list"))
