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

class ListStrIoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val variable = ListStrIoVariable[IO](Vector("a", "b", "c"))

    val invalidProperties = Map(
      "type" -> Value.Str("list-str"),
      "domain" -> Value.ListValue(List(Value.Integer(1)))
    )

    val validProperties = Map(
      "type" -> Value.Str("list-str"),
      "domain" -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

  "valueForIndex" should:
    "return the correct value for a valid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(Index(1)).asserting(_ mustEqual "b")

    "raise an AssertionError for an invalid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(Index(3)).assertThrows[AssertionError]

  "indexForValue" should:
    "return the correct index for a valid value" in newCase[CaseData]: data =>
      data.variable.indexForValue("b").asserting(_ mustEqual Index(1))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: data =>
      data.variable.indexForValue("d").assertThrows[AssertionError]

  "toProperties" should:
    "return the correct properties map" in newCase[CaseData]: data =>
      data.variable
        .toProperties
        .logValue
        .asserting(_ mustEqual Map(
          "type" -> Value.Str("list-str"),
          "domain" -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
        ))

  "fromProperties" should:
    "create ListStrIoVariable from valid properties" in newCase[CaseData]: data =>
      ListStrIoVariable.fromProperties[IO](data.validProperties)
        .logValue
        .asserting(_.elements mustEqual Vector("a", "b", "c"))

    "raise an AssertionError for invalid properties" in newCase[CaseData]: data =>
      ListStrIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue
        .assertThrows[AssertionError]
