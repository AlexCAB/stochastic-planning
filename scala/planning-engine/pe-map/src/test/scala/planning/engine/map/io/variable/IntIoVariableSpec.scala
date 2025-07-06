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
import planning.engine.common.values.node.IoIndex
import planning.engine.map.io.variable.IoVariable.*

class IntIoVariableSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val variable = IntIoVariable[IO](0, 10)

    val invalidProperties = Map(
      PROP.VAR_TYPE -> Value.Str(PROP_VALUE.INT_TYPE),
      PROP.MIN -> Value.Integer(0),
      PROP.MAX -> Value.Str("invalid")
    )

    val validProperties = Map(
      PROP.VAR_TYPE -> Value.Str(PROP_VALUE.INT_TYPE),
      PROP.MIN -> Value.Integer(0),
      PROP.MAX -> Value.Integer(10)
    )

    val validParameters = validProperties.map:
      case (k, v) => k -> v.toParam

  "IntIoVariable.valueForIndex(...)" should:
    "return the correct value for a valid index" in newCase[CaseData]: (_, data) =>
      data.variable.valueForIndex(IoIndex(5)).asserting(_ mustEqual 5)

    "raise an AssertionError for an invalid index" in newCase[CaseData]: (_, data) =>
      data.variable.valueForIndex(IoIndex(11)).assertThrows[AssertionError]

  "IntIoVariable.indexForValue(...)" should:
    "return the correct index for a valid value" in newCase[CaseData]: (_, data) =>
      data.variable.indexForValue(5).asserting(_ mustEqual IoIndex(5))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: (_, data) =>
      data.variable.indexForValue(11).assertThrows[AssertionError]

  "IntIoVariable.toQueryParams" should:
    "return the correct properties map" in newCase[CaseData]: (tn, data) =>
      data.variable
        .toQueryParams
        .logValue(tn)
        .asserting(_ mustEqual data.validParameters)

  "IntIoVariable.fromProperties(...)" should:
    "create IntIoVariable from valid properties" in newCase[CaseData]: (tn, data) =>
      IntIoVariable.fromProperties[IO](data.validProperties)
        .logValue(tn)
        .asserting(v => (v.min, v.max) mustEqual (0, 10))

    "raise an AssertionError for invalid properties" in newCase[CaseData]: (tn, data) =>
      IntIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue(tn)
        .assertThrows[AssertionError]
