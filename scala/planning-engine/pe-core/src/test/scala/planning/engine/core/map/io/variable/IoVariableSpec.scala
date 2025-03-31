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

class IoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val boolProperties = Map(
      "type" -> Value.Str("bool"),
      "domain" -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val intProperties = Map(
      "type" -> Value.Str("int"),
      "min" -> Value.Integer(0),
      "max" -> Value.Integer(10)
    )

    val floatProperties = Map(
      "type" -> Value.Str("float"),
      "min" -> Value.Decimal(0.0),
      "max" -> Value.Decimal(10.0)
    )

    val listStrProperties = Map(
      "type" -> Value.Str("list-str"),
      "domain" -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

    val invalidTypeProperties = Map(
      "type" -> Value.Str("unknown")
    )

    val missingTypeProperties = Map(
      "domain" -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

  "fromProperties" should:
    "create BooleanIoVariable from valid bool properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.boolProperties)
        .logValue
        .asserting(_ mustBe a[BooleanIoVariable[IO]])

    "create IntIoVariable from valid int properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.intProperties)
        .logValue
        .asserting(_ mustBe a[IntIoVariable[IO]])

    "create FloatIoVariable from valid float properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.floatProperties)
        .logValue
        .asserting(_ mustBe a[FloatIoVariable[IO]])

    "create ListStrIoVariable from valid list-str properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.listStrProperties)
        .logValue
        .asserting(_ mustBe a[ListStrIoVariable[IO]])

    "raise an AssertionError for invalid type properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.invalidTypeProperties)
        .logValue
        .assertThrows[AssertionError]

    "raise an AssertionError for missing type properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.missingTypeProperties)
        .logValue
        .assertThrows[AssertionError]
