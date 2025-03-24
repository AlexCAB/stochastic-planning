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


class IntIoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val variable = IntIoVariable[IO](0, 10)

    val invalidProperties = Map(
      "type" -> Value.Str("int"),
      "min" -> Value.Integer(0),
      "max" -> Value.Str("invalid"))

    val validProperties = Map(
      "type" -> Value.Str("int"),
      "min" -> Value.Integer(0),
      "max" -> Value.Integer(10))

  "valueForIndex" should:
    "return the correct value for a valid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(Index(5)).asserting(_ mustEqual 5)

    "raise an AssertionError for an invalid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(Index(11)).assertThrows[AssertionError]

  "indexForValue" should:
    "return the correct index for a valid value" in newCase[CaseData]: data =>
      data.variable.indexForValue(5).asserting(_ mustEqual Index(5))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: data =>
      data.variable.indexForValue(11).assertThrows[AssertionError]

  "toProperties" should:
    "return the correct properties map" in newCase[CaseData]: data =>
      data.variable
        .toProperties
        .logValue
        .asserting(_ mustEqual Map(
          "type" -> Value.Str("int"),
          "min" -> Value.Integer(0),
          "max" -> Value.Integer(10)))

  "fromProperties" should:
    "create IntIoVariable from valid properties" in newCase[CaseData]: data =>
      IntIoVariable.fromProperties[IO](data.validProperties)
        .logValue
        .asserting(v => (v.min, v.max ) mustEqual (0, 10))


    "raise an AssertionError for invalid properties" in newCase[CaseData]: data =>
      IntIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue
        .assertThrows[AssertionError]
