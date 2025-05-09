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

class FloatIoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val variable = FloatIoVariable[IO](0.0f, 10.0f)

    val invalidProperties = Map(
      "type" -> Value.Str("float"),
      "min" -> Value.Decimal(0.0),
      "max" -> Value.Str("invalid")
    )

    val validProperties = Map(
      "type" -> Value.Str("float"),
      "min" -> Value.Decimal(0.0),
      "max" -> Value.Decimal(10.0)
    )

  "valueForIndex" should:
    "return the correct value for a valid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(Index(50000)).asserting(_ mustEqual 5.0f)

    "raise an AssertionError for an invalid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(Index(110000)).assertThrows[AssertionError]

  "indexForValue" should:
    "return the correct index for a valid value" in newCase[CaseData]: data =>
      data.variable.indexForValue(5.0f).asserting(_ mustEqual Index(50000))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: data =>
      data.variable.indexForValue(11.0f).assertThrows[AssertionError]

  "toProperties" should:
    "return the correct properties map" in newCase[CaseData]: data =>
      data.variable
        .toProperties
        .logValue
        .asserting(_ mustEqual Map(
          "type" -> Value.Str("float"),
          "min" -> Value.Decimal(0.0),
          "max" -> Value.Decimal(10.0)
        ))

  "fromProperties" should:
    "create FloatIoVariable from valid properties" in newCase[CaseData]: data =>
      FloatIoVariable.fromProperties[IO](data.validProperties)
        .logValue
        .asserting(v => (v.min, v.max) mustEqual (0.0f, 10.0f))

    "raise an AssertionError for invalid properties" in newCase[CaseData]: data =>
      FloatIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue
        .assertThrows[AssertionError]
