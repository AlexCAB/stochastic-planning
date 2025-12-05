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

package planning.engine.map.io.variable

import cats.effect.IO
import planning.engine.common.UnitSpecWithData
import neotypes.model.types.Value
import planning.engine.common.properties.PROP
import planning.engine.common.properties.PropertiesMapping.*
import planning.engine.common.values.io.IoIndex
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.FLOAT_TYPE

class FloatIoVariableSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val variable = FloatIoVariable[IO](0.0f, 10.0f)

    val invalidProperties = Map(
      PROP.VAR_TYPE -> Value.Str(FLOAT_TYPE),
      PROP.MIN -> Value.Decimal(0.0),
      PROP.MAX -> Value.Str("invalid")
    )

    val validProperties = Map(
      PROP.VAR_TYPE -> Value.Str(FLOAT_TYPE),
      PROP.MIN -> Value.Decimal(0.0),
      PROP.MAX -> Value.Decimal(10.0)
    )

    val validParameters = validProperties.map:
      case (k, v) => k -> v.toParam

  "FloatIoVariable.valueForIndex(...)" should:
    "return the correct value for a valid index" in newCase[CaseData]: (_, data) =>
      data.variable.valueForIndex(IoIndex(50000)).asserting(_ mustEqual 5.0f)

    "raise an AssertionError for an invalid index" in newCase[CaseData]: (_, data) =>
      data.variable.valueForIndex(IoIndex(110000)).assertThrows[AssertionError]

  "FloatIoVariable.indexForValue(...)" should:
    "return the correct index for a valid value" in newCase[CaseData]: (_, data) =>
      data.variable.indexForValue(5.0f).asserting(_ mustEqual IoIndex(50000))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: (_, data) =>
      data.variable.indexForValue(11.0f).assertThrows[AssertionError]

  "FloatIoVariable.toQueryParams" should:
    "return the correct properties map" in newCase[CaseData]: (tn, data) =>
      data.variable
        .toQueryParams
        .logValue(tn)
        .asserting(_ mustEqual data.validParameters)

  "FloatIoVariable.fromProperties(...)" should:
    "create FloatIoVariable from valid properties" in newCase[CaseData]: (tn, data) =>
      FloatIoVariable.fromProperties[IO](data.validProperties)
        .logValue(tn)
        .asserting(v => (v.min, v.max) mustEqual (0.0f, 10.0f))

    "raise an AssertionError for invalid properties" in newCase[CaseData]: (tn, data) =>
      FloatIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue(tn)
        .assertThrows[AssertionError]
