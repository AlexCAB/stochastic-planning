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
