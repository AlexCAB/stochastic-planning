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
| created: 2025-03-24 |||||||||||*/

package planning.engine.core.map.io.variable

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import neotypes.model.types.Value
import planning.engine.common.values.Index

class BooleanIoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val trueAcceptable = BooleanIoVariable[IO](Set(true))
    val falseAcceptable = BooleanIoVariable[IO](Set(false))
    val bothAcceptable = BooleanIoVariable[IO](Set(true, false))

    val invalidProperties = Map(
      "type" -> Value.Str("bool"),
      "domain" -> Value.ListValue(List(Value.Str("invalid"))))

    val validProperties = Map(
      "type" -> Value.Str("bool"),
      "domain" -> Value.ListValue(List(Value.Bool(true), Value.Bool(false))))

  "valueForIndex" should:
    "return false for index 0 if false is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.falseAcceptable.valueForIndex(Index(0)).asserting(_ mustEqual false)
      data.trueAcceptable.valueForIndex(Index(0)).assertThrows[AssertionError]

    "return true for index 1 if true is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.trueAcceptable.valueForIndex(Index(1)).asserting(_ mustEqual true)
      data.falseAcceptable.valueForIndex(Index(1)).assertThrows[AssertionError]

    "return true or false for index 1 or 0" in newCase[CaseData]: data =>
      data.bothAcceptable.valueForIndex(Index(0)).asserting(_ mustEqual false)
      data.bothAcceptable.valueForIndex(Index(1)).asserting(_ mustEqual true)

    "raise an AssertionError for invalid index" in newCase[CaseData]: data =>
      data.trueAcceptable.valueForIndex(Index(2)).assertThrows[AssertionError]

  "indexForValue" should:
    "return Index(0) for false if false is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.falseAcceptable.indexForValue(false).asserting(_ mustEqual Index(0))
      data.trueAcceptable.indexForValue(false).assertThrows[AssertionError]

    "return Index(1) for true if true is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.trueAcceptable.indexForValue(true).asserting(_ mustEqual Index(1))
      data.falseAcceptable.indexForValue(true).assertThrows[AssertionError]

    "return Index(0) or Index(1) for false or true" in newCase[CaseData]: data =>
      data.bothAcceptable.indexForValue(false).asserting(_ mustEqual Index(0))
      data.bothAcceptable.indexForValue(true).asserting(_ mustEqual Index(1))

  "toProperties" should:
    "return correct properties map" in newCase[CaseData]: data =>
      data.bothAcceptable.toProperties.logValue
        .asserting(
          _ mustEqual Map(
            "type" -> Value.Str("bool"),
            "domain" -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
          )
        )

  "fromProperties" should:
    "create BooleanIoVariable from valid properties" in newCase[CaseData]: data =>
      BooleanIoVariable
        .fromProperties[IO](data.validProperties)
        .logValue
        .asserting(_.acceptableValues mustEqual Set(true, false))

    "raise an AssertionError for invalid domain value" in newCase[CaseData]: data =>
      BooleanIoVariable
        .fromProperties[IO](data.invalidProperties)
        .logValue
        .assertThrows[AssertionError]
