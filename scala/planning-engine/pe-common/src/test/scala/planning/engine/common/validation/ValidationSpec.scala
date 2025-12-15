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
| created: 2025-06-30 |||||||||||*/

package planning.engine.common.validation

import cats.effect.IO
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData

class ValidationSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val validObj: Validation = new Validation:
      def validationName = "validObj"
      def validationErrors = Nil

    val invalidObj: Validation = new Validation:
      def validationName = "invalidObj"
      def validationErrors = validations(
        (false, "Error 1"),
        (false, "Error 2")
      )

  "Validation.validate" should:
    "complete successfully when there are no validation errors" in newCase[CaseData]: (_, data) =>
      Validation.validate[IO](data.validObj).asserting(_ mustEqual (()))

    "raise an error when there are validation errors" in newCase[CaseData]: (_, data) =>
      Validation.validate[IO](data.invalidObj).assertThrows[ValidationError]

  "Validation.validateList" should:
    "complete successfully when all objects have no validation errors" in newCase[CaseData]: (_, data) =>
      Validation.validateList[IO](List(data.validObj, data.validObj)).asserting(_ mustEqual (()))

    "raise an error when at least one object has validation errors" in newCase[CaseData]: (_, data) =>
      Validation.validateList[IO](List(data.validObj, data.invalidObj)).assertThrows[ValidationError]

  "Validation.ex.isDistinct(...)" should:
    "return true when all elements are distinct" in newCase[CaseData]: (_, _) =>
      val obj = new Validation:
        def validationName = "testObj"
        def validationErrors = validations(
          List(1, 2, 3).isDistinct("Elements are distinct"),
          List(1, 2, 2).isDistinct("Elements are not distinct")
        )
      obj.pure[IO].asserting(_
        .validationErrors.map(_.getMessage) mustBe List("Elements are not distinct, duplicates: 2"))

  "Validation.ex.containsAllOf(...)" should:
    "return true when all elements are contained" in newCase[CaseData]: (_, _) =>
      val obj = new Validation:
        def validationName = "testObj"
        def validationErrors = validations(
          List(1, 2, 3).containsAllOf(List(2, 3), "All elements are contained"),
          List(1, 2, 3).containsAllOf(List(2, 4), "Not all elements are contained")
        )
      obj.pure[IO].asserting(_
        .validationErrors.map(_.getMessage) mustBe List("Not all elements are contained, missing elements: 4"))

  "Validation.ex.haveSameElems(...)" should:
    "return true when both collections have the same elements" in newCase[CaseData]: (_, _) =>
      val obj = new Validation:
        def validationName = "testObj"
        def validationErrors = validations(
          List(1, 2, 3).haveSameElems(List(3, 2, 1), "Collections have the same elements"),
          List(1, 2, 3).haveSameElems(List(2, 4), "Collections do not have the same elements")
        )
      obj.pure[IO].asserting(_.validationErrors
        .map(_.getMessage) mustBe List("Collections do not have the same elements, not same elements: 1, 3, 4"))
